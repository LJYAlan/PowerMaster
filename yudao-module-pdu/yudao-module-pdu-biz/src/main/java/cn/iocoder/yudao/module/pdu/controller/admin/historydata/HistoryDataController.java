package cn.iocoder.yudao.module.pdu.controller.admin.historydata;

import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataDetailsReqVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;

@Tag(name = "管理后台 - pdu历史数据")
@RestController
@RequestMapping("/pdu/history-data")
@Validated
public class HistoryDataController {

    @Resource
    private HistoryDataService historyDataService;


    @GetMapping("/page")
    @Operation(summary = "获得pdu历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = historyDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得pdu历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(HistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = historyDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }






//    @GetMapping("/export-excel")
//    @Operation(summary = "导出pdu历史数据 Excel")
////    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
//    @OperateLog(type = EXPORT)
//    public void exportHistoryDataExcel(@Valid HistoryDataPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<PduHdaTotalRealtimeDO> list = historyDataService.getHistoryDataPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "pdu历史数据.xls", "数据", HistoryDataRespVO.class,
//                        BeanUtils.toBean(list, HistoryDataRespVO.class));
//    }

}