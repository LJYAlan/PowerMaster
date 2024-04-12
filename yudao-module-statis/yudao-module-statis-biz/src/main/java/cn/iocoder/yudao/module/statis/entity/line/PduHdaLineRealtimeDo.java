package cn.iocoder.yudao.module.statis.entity.line;

import cn.iocoder.yudao.module.statis.entity.BaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu相历史数据表（实时）
 */
@Data
public class PduHdaLineRealtimeDo extends BaseDo {


    /**
     * 相
     */
    @JsonProperty("line_id")
    private int lineId;

    /**
     * 电压
     */
    @JsonProperty("vol_value")
    private float vol;

    /**
     * 电流
     */
    @JsonProperty("cur_value")
    private float cur;

    /**
     * 有功功率
     */
    @JsonProperty("pow_active")
    private float activePow;

    /**
     * 视在功率
     */
    @JsonProperty("pow_apparent")
    private float apparentPow;

    /**
     * 功率因素
     */
    @JsonProperty("power_factor")
    private int powerFactor;

}
