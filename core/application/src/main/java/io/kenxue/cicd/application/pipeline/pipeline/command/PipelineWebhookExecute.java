package io.kenxue.cicd.application.pipeline.pipeline.command;

import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.coreclient.dto.pipeline.pipeline.PipelineExecuteCmd;
import io.kenxue.cicd.coreclient.dto.pipeline.pipeline.PipelineWebhookExecuteCmd;
import io.kenxue.cicd.domain.domain.pipeline.Pipeline;
import io.kenxue.cicd.domain.repository.pipeline.PipelineRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PipelineWebhookExecute {
    @Resource
    private PipelineRepository pipelineRepository;
    @Resource
    private PipelineExecuteCmdExe pipelineExecuteCmdExe;

    public Response execute(PipelineWebhookExecuteCmd cmd) {

        Pipeline pipeline = pipelineRepository.getByUUID(cmd.getUuid());

        return pipelineExecuteCmdExe.execute(new PipelineExecuteCmd().setId(pipeline.getId()));
    }
}