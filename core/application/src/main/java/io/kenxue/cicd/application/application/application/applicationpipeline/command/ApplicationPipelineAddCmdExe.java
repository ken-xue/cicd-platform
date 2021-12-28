package io.kenxue.cicd.application.application.application.applicationpipeline.command;

import io.kenxue.cicd.application.application.application.applicationpipeline.assembler.ApplicationPipeline2DTOAssembler;
import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.domain.repository.application.ApplicationPipelineRepository;
import io.kenxue.cicd.domain.domain.application.ApplicationPipeline;
import io.kenxue.cicd.coreclient.dto.application.applicationpipeline.ApplicationPipelineAddCmd;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import io.kenxue.cicd.coreclient.context.UserThreadContext;
/**
 * 流水线
 * @author mikey
 * @date 2021-12-28 22:57:10
 */
@Component
public class ApplicationPipelineAddCmdExe {

    @Resource
    private ApplicationPipelineRepository applicationPipelineRepository;
    @Resource
    private ApplicationPipeline2DTOAssembler applicationPipeline2DTOAssembler;

    public Response execute(ApplicationPipelineAddCmd cmd) {
        ApplicationPipeline applicationPipeline = applicationPipeline2DTOAssembler.toDomain(cmd.getApplicationPipelineDTO());
        applicationPipeline.create(UserThreadContext.get());
        applicationPipelineRepository.create(applicationPipeline);
        return Response.success();
    }
}
