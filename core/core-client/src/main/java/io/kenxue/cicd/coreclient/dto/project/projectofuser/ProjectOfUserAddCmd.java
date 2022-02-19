package io.kenxue.cicd.coreclient.dto.project.projectofuser;

import io.kenxue.cicd.coreclient.dto.common.command.CommonCommand;
import lombok.Data;
import javax.validation.constraints.NotNull;
import lombok.experimental.Accessors;

/**
 * 项目关联用户
 * @author mikey
 * @date 2022-02-18 14:06:52
 */
@Data
@Accessors(chain = true)
public class ProjectOfUserAddCmd extends CommonCommand {
    @NotNull
    private ProjectOfUserDTO projectOfUserDTO;
}
