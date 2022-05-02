package io.kenxue.cicd.infrastructure.repositoryimpl.basic;

import io.kenxue.cicd.coreclient.dto.basic.zookeeper.ZkNodeAddCmd;
import io.kenxue.cicd.coreclient.dto.basic.zookeeper.ZkTreeNode;
import io.kenxue.cicd.coreclient.dto.basic.zookeeper.ZookeeperConnectCmd;
import io.kenxue.cicd.coreclient.dto.common.response.MultiResponse;
import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.coreclient.dto.common.response.SingleResponse;
import io.kenxue.cicd.coreclient.exception.BizException;
import io.kenxue.cicd.domain.repository.basic.ZookeeperRepository;
import io.kenxue.cicd.infrastructure.repositoryimpl.basic.factory.ZookeeperInstanceFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Component
public class ZookeeperRepositoryImpl implements ZookeeperRepository {

    static CuratorFramework instance;

    private static final String ROOT = "/CicdRoot";

    @Override
    public ZkTreeNode connect(ZookeeperConnectCmd zookeeperConnectCmd){
        try {
            instance = ZookeeperInstanceFactory.getInstance(zookeeperConnectCmd);
            List<String> list = instance.getChildren().forPath("/");
            List<ZkTreeNode> nodeList = buildTree(null,list);
            ZkTreeNode tree = new ZkTreeNode();
            tree.setId(ROOT);
            tree.setLabel(ROOT);
            tree.setChildren(nodeList);
            return tree;
        }catch (Exception e){
            throw new BizException("zk初始化异常");
        }

    }

    @Override
    public List<ZkTreeNode> lazyLeaf(String id) {
        try {
            List<String> list = instance.getChildren().forPath(id);
            return buildTree(id,list);
        }catch (Exception e){
            e.printStackTrace();
            throw new BizException("叶子节点懒加载失败");
        }
    }

    @Override
    public Response addZkNode(ZkNodeAddCmd nodeAddCmd) {
        try {
            String result = instance.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(nodeAddCmd.getName(),nodeAddCmd.getData().getBytes(StandardCharsets.UTF_8));
            return Response.success(result);
        }catch (Exception e){
            throw new BizException("添加节点失败");
        }

    }


    public List<ZkTreeNode> buildTree(String id,List<String> list){
        List<ZkTreeNode> nodeList = new ArrayList<>();
        list.forEach(node -> {
            ZkTreeNode treeNode = new ZkTreeNode();
            treeNode.setId("/"+node);
            treeNode.setLabel("/"+node);
            treeNode.setParentId(id == null ? "/" : id + "/" +node);
            try {
                treeNode.setParentFlag(instance.getChildren().forPath(id != null ? id : "/"+node).size() > 0);
            } catch (Exception e) {
                throw new BizException("获取子节点异常");
            }
            nodeList.add(treeNode);
        });
        return nodeList;
    }


}