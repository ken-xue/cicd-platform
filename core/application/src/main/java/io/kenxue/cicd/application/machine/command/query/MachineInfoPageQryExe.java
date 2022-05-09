package io.kenxue.cicd.application.machine.command.query;

import io.kenxue.cicd.application.machine.assembler.MachineInfo2DTOAssembler;
import io.kenxue.cicd.coreclient.dto.machine.MachineInfoDTO;
import io.kenxue.cicd.coreclient.dto.machine.MachineInfoPageQry;
import io.kenxue.cicd.coreclient.dto.common.response.PageResponse;
import io.kenxue.cicd.domain.repository.machine.MachineInfoRepository;
import org.springframework.stereotype.Component;
import io.kenxue.cicd.domain.domain.machine.MachineInfo;
import javax.annotation.Resource;
import io.kenxue.cicd.coreclient.dto.common.page.Page;
/**
 * 服务器节点
 * @author mikey
 * @date 2022-02-07 17:55:06
 */
@Component
public class MachineInfoPageQryExe {

    @Resource
    private MachineInfoRepository machineInfoRepository;
    @Resource
    private MachineInfo2DTOAssembler machineInfo2DTOAssembler;

    public PageResponse<MachineInfoDTO> execute(MachineInfoPageQry qry) {
        Page<MachineInfo> page = machineInfoRepository.page(qry);
        return PageResponse.of(machineInfo2DTOAssembler.toDTOPage(page));
    }
}
