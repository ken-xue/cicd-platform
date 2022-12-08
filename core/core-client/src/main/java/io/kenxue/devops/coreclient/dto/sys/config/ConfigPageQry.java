package io.kenxue.devops.coreclient.dto.sys.config;

import io.kenxue.devops.coreclient.dto.common.command.PageQuery;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统配置表
 * @author 麦奇
 * @date 2022-12-08 19:34:34
 */
@Data
@Accessors(chain = true)
public class ConfigPageQry extends PageQuery {

    private ConfigDTO configDTO;

}
