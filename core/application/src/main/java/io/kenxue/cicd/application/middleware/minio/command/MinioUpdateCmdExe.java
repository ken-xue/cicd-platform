package io.kenxue.cicd.application.middleware.minio.command;

import io.kenxue.cicd.application.middleware.minio.assembler.Minio2DTOAssembler;
import io.kenxue.cicd.domain.domain.middleware.Minio;
import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.coreclient.dto.middleware.minio.MinioUpdateCmd;
import io.kenxue.cicd.domain.repository.middleware.MinioRepository;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
/**
 * minio实例
 * @author 麦奇
 * @date 2022-05-25 23:50:28
 */
@Component
public class MinioUpdateCmdExe {

    @Resource
    private MinioRepository minioRepository;
    @Resource
    private Minio2DTOAssembler minio2DTOAssembler;

    public Response execute(MinioUpdateCmd cmd) {
        Minio minio = minio2DTOAssembler.toDomain(cmd.getMinioDTO());
        minioRepository.update(minio);
        return Response.success();
    }
}