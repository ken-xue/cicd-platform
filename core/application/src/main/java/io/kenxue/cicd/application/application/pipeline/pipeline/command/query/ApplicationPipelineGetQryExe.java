package io.kenxue.cicd.application.application.pipeline.pipeline.command.query;

import io.kenxue.cicd.application.application.pipeline.pipeline.assembler.ApplicationPipeline2DTOAssembler;
import io.kenxue.cicd.coreclient.dto.pipeline.pipeline.ApplicationPipelineDTO;
import io.kenxue.cicd.coreclient.dto.common.response.SingleResponse;
import io.kenxue.cicd.coreclient.dto.pipeline.pipeline.ApplicationPipelineGetQry;
import io.kenxue.cicd.domain.repository.application.ApplicationPipelineRepository;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
/**
 * 流水线
 * @author mikey
 * @date 2021-12-28 22:57:10
 */
@Component
public class ApplicationPipelineGetQryExe {

    @Resource
    private ApplicationPipelineRepository applicationPipelineRepository;
    @Resource
    private ApplicationPipeline2DTOAssembler applicationPipeline2DTOAssembler;

    public SingleResponse<ApplicationPipelineDTO> execute(ApplicationPipelineGetQry qry) {
        return SingleResponse.of(applicationPipeline2DTOAssembler.toDTO(applicationPipelineRepository.getById(qry.getId())));
    }

}