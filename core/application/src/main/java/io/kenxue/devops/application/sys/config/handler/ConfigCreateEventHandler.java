package io.kenxue.devops.application.sys.config.handler;

import io.kenxue.devops.application.common.event.EventHandler;
import io.kenxue.devops.application.common.event.EventHandlerI;
import io.kenxue.devops.coreclient.dto.common.response.Response;
import io.kenxue.devops.coreclient.dto.sys.config.event.ConfigCreateEvent;
import lombok.extern.slf4j.Slf4j;
/**
 * 系统配置表
 * @author 麦奇
 * @date 2022-12-08 19:34:34
 */
@Slf4j
@EventHandler
public class ConfigCreateEventHandler implements EventHandlerI<Response, ConfigCreateEvent> {

    public Response execute(ConfigCreateEvent event) {
        log.debug("Handling Event:{}",event);
        return Response.success();
    }
}
