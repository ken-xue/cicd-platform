package io.kenxue.cicd.application.middleware.zookeeper.command;

import io.kenxue.cicd.application.middleware.zookeeper.factory.ZkCacheFactory;
import io.kenxue.cicd.coreclient.dto.common.response.Response;
import io.kenxue.cicd.coreclient.dto.middleware.zookeeper.node.ZookeeperNodeUpdateCmd;
import io.kenxue.cicd.coreclient.exception.ZkException;
import io.kenxue.cicd.coreclient.exception.code.ZkErrorCode;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

/**
 * @author 刘牌
 * @Title:
 * @date 2022-05-1816:06
 */
@Component
public class ZookeeperNodeUpdateCmdExe {

    public Response execute(ZookeeperNodeUpdateCmd zookeeperNodeUpdateCmd) {
        try {
            CuratorFramework framework = ZkCacheFactory.getCuratorFramework(zookeeperNodeUpdateCmd.getZookeeperAddOrUpdateNodeDTO().getUuid());
            framework.setData().forPath(zookeeperNodeUpdateCmd.getZookeeperAddOrUpdateNodeDTO().getName());
        }catch (Exception e){
            throw new ZkException(ZkErrorCode.UPDATE_ERROR);
        }
        return Response.success();
    }
}
