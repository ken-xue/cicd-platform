package io.kenxue.cicd.application.pipeline.pipeline.command;

import com.alibaba.fastjson.JSON;
import io.kenxue.cicd.application.common.event.EventBusI;
import io.kenxue.cicd.application.pipeline.pipeline.manager.NodeManager;
import io.kenxue.cicd.coreclient.context.UserThreadContext;
import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.coreclient.dto.common.response.SingleResponse;
import io.kenxue.cicd.coreclient.dto.pipeline.pipeline.PipelineExecuteCmd;
import io.kenxue.cicd.coreclient.dto.pipeline.pipeline.event.PipelineNodeRefreshEvent;
import io.kenxue.cicd.domain.domain.pipeline.Pipeline;
import io.kenxue.cicd.domain.domain.pipeline.PipelineExecuteLogger;
import io.kenxue.cicd.domain.domain.pipeline.PipelineNodeInfo;
import io.kenxue.cicd.domain.factory.pipeline.PipelineExecuteLoggerFactory;
import io.kenxue.cicd.domain.repository.pipeline.PipelineRepository;
import io.kenxue.cicd.domain.repository.pipeline.PipelineExecuteLoggerRepository;
import io.kenxue.cicd.domain.repository.pipeline.PipelineNodeInfoRepository;
import io.kenxue.cicd.sharedataboject.pipeline.context.DefaultResult;
import io.kenxue.cicd.sharedataboject.pipeline.context.ExecuteContext;
import io.kenxue.cicd.sharedataboject.pipeline.context.Result;
import io.kenxue.cicd.sharedataboject.pipeline.enums.NodeEnum;
import io.kenxue.cicd.sharedataboject.pipeline.enums.NodeExecuteStatus;
import io.kenxue.cicd.sharedataboject.pipeline.graph.Graph;
import io.kenxue.cicd.sharedataboject.pipeline.graph.Nodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 流水线
 *
 * @author mikey
 * @date 2021-12-28 22:57:10
 */
@Slf4j
@Component
public class PipelineExecuteCmdExe implements DisposableBean {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 3, 20L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    //当前正在执行的实例 k=执行记录uuid,v=pipeline
    private static volatile ConcurrentHashMap<String, Pipeline> executingPipelineMap = new ConcurrentHashMap<>(2 << 4);

    private volatile List<String> edges;//执行路线

    private volatile Nodes start;

    private volatile Map<String, List<String>> targetLineMap = new HashMap<>(2 << 4);//路线source->List<target>

    private volatile Map<String, List<String>> sourceLineMap = new HashMap<>(2 << 4);//路线target<-List<source>

    private volatile Map<String, Nodes> targetMap = new HashMap<>(2 << 4);//

    private volatile Map<String, Nodes> sourceMap = new HashMap<>(2 << 4);//

    private volatile PipelineExecuteLogger pipelineExecuteLogger;//当前执行的记录

    @Resource
    private PipelineExecuteLoggerRepository pipelineExecuteLoggerRepository;
    @Resource
    private PipelineRepository pipelineRepository;
    @Resource
    private PipelineNodeInfoRepository pipelineNodeInfoRepository;
    @Resource
    private NodeManager nodeManager;
    @Resource
    private EventBusI eventBus;

    /**
     * 两个入口
     * 1.打开页面点击执行
     * 1.1 生成执行记录id
     * 1.2 建立节点实时状态socket推送
     * 2.点击执行记录
     * 2.1 执行中（根据执行记录id判断是否正在执行，如果是正在执行建立socket连接）
     * 2.2 已经执行完成（返回执行记录结果即可，无需建立socket连接）
     *
     * @param cmd
     * @return
     */
    public Response execute(PipelineExecuteCmd cmd) {

        Pipeline pipeline = pipelineRepository.getById(cmd.getId());

        ExecuteContext context = buildContext(pipeline);

        prepare(pipeline);

        generateExecutionRecord(pipeline);

        executor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            execute(context, start);
        });

        return SingleResponse.of(pipelineExecuteLogger);
    }

    /**
     * 生成执行记录
     *
     * @param pipeline
     */
    private PipelineExecuteLogger generateExecutionRecord(Pipeline pipeline) {
        pipelineExecuteLogger = PipelineExecuteLoggerFactory.getPipelineExecuteLogger();
        pipelineExecuteLogger.create(UserThreadContext.get());
        pipelineExecuteLogger.setPipelineUuid(pipeline.getUuid());
        pipelineExecuteLogger.setExecuteStartTime(new Date());
        pipelineExecuteLogger.setGraphContent(JSON.toJSONString(pipeline.getGraph()));
        pipelineExecuteLoggerRepository.create(pipelineExecuteLogger);
        executingPipelineMap.put(pipeline.getUuid(), pipeline);
        return pipelineExecuteLogger;
    }

    /**
     * 构建上下文
     *
     * @param pipeline
     */
    private ExecuteContext buildContext(Pipeline pipeline) {

        ExecuteContext context = new ExecuteContext();

        for (Nodes node : pipeline.getGraph().getNodes()) {
            if (NodeEnum.START.getName().equals(node.getName()) || NodeEnum.END.getName().equals(node.getName()))
                continue;
            PipelineNodeInfo nodeInfo = pipelineNodeInfoRepository.getByNodeId(node.getId());
            if (Objects.isNull(nodeInfo)) {
                log.error("node : {},config node info data is null", node);
            } else {
                context.setAttributes(node.getName(), nodeInfo);
            }
        }

        return context;
    }

    /**
     * 解析流水线准备数据
     *
     * @param pipeline
     */
    private void prepare(Pipeline pipeline) {

        targetLineMap.clear();
        sourceLineMap.clear();
        targetMap.clear();
        sourceMap.clear();

        Graph graph = pipeline.getGraph();
        List<Nodes> nodes = graph.getNodes();
        //获取开始节点
        start = nodes.stream().filter(node -> NodeEnum.START.toString().equals(node.getName())).findFirst().get();
        //移除开始节点
        nodes.remove(start);
        //映射每个节点对应的输入和输出节点
        for (Nodes n : nodes) {
            //当前节点n作为target
            n.getPoints().getTargets().forEach(uuid -> targetMap.put(uuid.replace("target-", ""), n));
            //当前节点n作为source
            n.getPoints().getSources().forEach(uuid -> sourceMap.put(uuid.replace("source-", ""), n));
        }
        //获取执行路径
        edges = graph.getEdges();
        for (String edge : edges) {
            String[] lines = edge.split("&&");
            String source = lines[0].replace("source-", "");
            String target = lines[1].replace("target-", "");
            //target
            List<String> targetLineMapOrDefault = targetLineMap.getOrDefault(source, new LinkedList<>());
            targetLineMapOrDefault.add(target);
            targetLineMap.put(source, targetLineMapOrDefault);
            //source
            List<String> sourceLineMapOrDefault = sourceLineMap.getOrDefault(target, new LinkedList<>());
            sourceLineMapOrDefault.add(source);
            sourceLineMap.put(target, sourceLineMapOrDefault);
        }
    }


    /**
     * 执行流水线
     *
     * @param context
     * @param node
     */
    public void execute(ExecuteContext context, Nodes node) {

        //判断是否可执行
        if (!executable(node)) return;

        try {
            log.info("执行的节点：{}", node.getName());
            //执行结果
            Result result = new DefaultResult();
            //变更状态
            node.refreshStatus(NodeExecuteStatus.LOADING);//进行中
            //发送事件
            eventBus.publish(new PipelineNodeRefreshEvent(pipelineExecuteLogger.getUuid(), node, sourceLineMap, NodeExecuteStatus.LOADING));
            //获取下一个执行的路线
            List<String> sources = node.getPoints().getSources();
            //执行
            Result ret = nodeManager.get(node.getName()).execute(context);
            result.add(node.getName(), ret);
            node.refreshStatus(NodeExecuteStatus.SUCCESS);//执行成功
            sources.forEach(sce -> {
                String next = sce.replace("source-", "");
                List<String> list = targetLineMap.getOrDefault(next, Collections.emptyList());
                list.forEach(v -> executor.submit(() -> execute(context, targetMap.get(v))));
            });
        } catch (Exception e) {
            node.refreshStatus(NodeExecuteStatus.FAILED);//执行失败
            log.error("execute error , cur node : {}", node);
            e.printStackTrace();
        }
        //推送节点状态和所有输出的边
        eventBus.publish(new PipelineNodeRefreshEvent(pipelineExecuteLogger.getUuid(), node, targetLineMap));
    }

    /**
     * 判断是否可执行当前节点（检查当前节点是否所有输入节点都已经执行完成）
     * @param node 当前节点
     * @return
     */
    private synchronized boolean executable(Nodes node) {
        try {
            log.info("检查是否所有输入节点都已经执行完成:{}", node.getName());
            if (NodeExecuteStatus.SUCCESS.getName().equals(node.getData().getNodeState())) return false;
            //判断当前节点的所有前置节点是否已经执行完成
            List<String> targets = node.getPoints().getTargets();
            for (String t : targets) {
                String targetUUID = t.replace("target-", "");
                List<String> sourceUUIDList = sourceLineMap.getOrDefault(targetUUID, Collections.emptyList());
                for (String sourceUUID : sourceUUIDList) {
                    Nodes sourceNode = sourceMap.get(sourceUUID);
                    if (Objects.nonNull(sourceNode) && (Objects.isNull(sourceNode.getData().getNodeState()) ||
                            StringUtils.isBlank(sourceNode.getData().getNodeState()) ||
                            NodeExecuteStatus.LOADING.getName().equals(sourceNode.getData().getNodeState()) ||
                            NodeExecuteStatus.FAILED.getName().equals(sourceNode.getData().getNodeState()))) {
                        log.info("输入节点:{} 未执行完成，放弃执行当前节点 {}", sourceNode, node);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Pipeline get(String key) {
        return executingPipelineMap.get(key);
    }

    @Override
    public void destroy() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}