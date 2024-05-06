package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import cn.iocoder.yudao.module.cabinet.vo.CabinetVo;
import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜主页面
 * @date 2024/4/28 11:12
 */
@RestController
public class CabinetController {

    @Autowired
    CabinetService cabinetService;


    /**
     * 机柜主页面
     * @param pageReqVO
     */
    @PostMapping("/cabinet/page")
    public CommonResult<PageResult<JSONObject>> getCabinetPage(@RequestBody CabinetIndexVo pageReqVO)  {
        PageResult<JSONObject> pageResult = cabinetService.getPageCabinet(pageReqVO);
        return success(pageResult);
    }

    /**
     * 机柜详情
     * @param id 机柜id
     */
    @GetMapping("/cabinet/detail")
    public CommonResult<JSONObject> getCabinetDetail(@Param("id") int id)  {
        JSONObject dto = cabinetService.getCabinetDetail(id);
        return success(dto);
    }

    /**
     * 机柜详情
     * @param id 机柜id
     */
    @GetMapping("/cabinet/detailV2")
    public CommonResult<CabinetDTO> getCabinetDetailV2(@Param("id") int id)  {
        CabinetDTO dto = cabinetService.getCabinetDetailV2(id);
        return success(dto);
    }


    /**
     * 机柜新增/编辑页面
     * @param vo
     */
    @PostMapping("/cabinet/save")
    public CommonResult saveCabinet(@RequestBody CabinetVo vo)  {
        CommonResult message = cabinetService.saveCabinet(vo);
        return message;
    }


    /**
     * 机柜删除
     * @param id 机柜id
     */
    @GetMapping("/cabinet/delete")
    public CommonResult<Integer> deleteCabinet(@Param("id") int id)  {
        int cabinetId = cabinetService.delCabinet(id);
        if (cabinetId == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(),"删除失败");
        }
        return success(cabinetId);
    }
}
