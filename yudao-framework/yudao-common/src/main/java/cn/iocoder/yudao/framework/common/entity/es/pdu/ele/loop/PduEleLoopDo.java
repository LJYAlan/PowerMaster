package cn.iocoder.yudao.framework.common.entity.es.pdu.ele.loop;

import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/8 15:03
 * @Description: pdu回路电能表
 */
@Data
public class PduEleLoopDo extends PduEleTotalRealtimeDo {


    /**
     * 回路
     */
    @JsonProperty("loop_id")
    private int loopId;

}
