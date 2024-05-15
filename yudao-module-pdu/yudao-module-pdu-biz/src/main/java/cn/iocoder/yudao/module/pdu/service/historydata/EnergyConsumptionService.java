package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataDetailsReqVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EnergyConsumptionService {
    /**
     * 获得pdu电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得pdu各输出位电量数据
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    List<Object> getOutletsEQData(EnergyConsumptionPageReqVO reqVO) throws IOException;
}
