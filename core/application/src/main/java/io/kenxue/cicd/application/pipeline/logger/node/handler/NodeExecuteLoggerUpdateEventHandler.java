package io.kenxue.cicd.application.pipeline.logger.node.handler;

import io.kenxue.cicd.application.common.event.EventHandler;
import io.kenxue.cicd.application.common.event.EventHandlerI;
import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.coreclient.dto.pipeline.nodeexecutelogger.event.NodeExecuteLoggerUpdateEvent;
import lombok.extern.slf4j.Slf4j;
/**
 * 节点执行记录
 * @author mikey
 * @date 2022-05-05 11:24:39
 */
@Slf4j
@EventHandler
public class NodeExecuteLoggerUpdateEventHandler implements EventHandlerI<Response, NodeExecuteLoggerUpdateEvent> {
    
    public Response execute(NodeExecuteLoggerUpdateEvent event) {
        log.debug("Handling Event:{}",event);
        return Response.success();
    }
}