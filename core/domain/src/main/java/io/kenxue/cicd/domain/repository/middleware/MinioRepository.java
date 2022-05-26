package io.kenxue.cicd.domain.repository.middleware;

import io.kenxue.cicd.coreclient.dto.common.page.Page;
import io.kenxue.cicd.coreclient.dto.middleware.minio.MinioListQry;
import io.kenxue.cicd.coreclient.dto.middleware.minio.MinioPageQry;
import io.kenxue.cicd.domain.domain.middleware.Minio;
import java.util.List;
/**
 * minio实例
 * @author 麦奇
 * @date 2022-05-25 23:50:28
 */
public interface MinioRepository {
    void create(Minio minio);
    void update(Minio minio);
    Minio getById(Long id);
    List<Minio> list(MinioListQry minioListQry);
    Page<Minio> page(MinioPageQry qry);
}
