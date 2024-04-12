package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletRealtimeDo;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.framework.common.enums.EsStatisFieldEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 09:35
 * @Description: 输出位历史数据统计
 */
@Slf4j
@Component
public class PduOutletDao {

    @Autowired
    RestHighLevelClient client;


    /**
     * 输出位历史数据统计(按小时)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Object, Map<Object, PduHdaOutletBaseDo>> statisOutletHour(String startTime, String endTime) {
        Map<Object, Map<Object, PduHdaOutletBaseDo>> result = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(EsIndexEnum.PDU_HDA_OUTLET_REALTIME.getIndex());
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime));

//            builder.query(QueryBuilders.matchAllQuery());
            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms("by_pdu")
                    .field("pdu_id");

            // 设置Avg指标聚合，按outlet_id分组
            TermsAggregationBuilder outletAggregationBuilder = AggregationBuilders.terms("by_outlet").field(OUTLET_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(outletAggregationBuilder
                    //统计平均电流
                    .subAggregation(AggregationBuilders.avg(CUR_AVG_VALUE).field(CUR))
                    //最大电流
                    .subAggregation(AggregationBuilders.max(CUR_MAX_VALUE).field(CUR))
                    //最小电流
                    .subAggregation(AggregationBuilders.min(CUR_MIN_VALUE).field(CUR))
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW))
                    //最大有功功率
                    .subAggregation(AggregationBuilders.max(ACTIVE_POW_MAX_VALUE).field(ACTIVE_POW))
                    // 最小有功功率
                    .subAggregation(AggregationBuilders.min(ACTIVE_POW_MIN_VALUE).field(ACTIVE_POW))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW))
                    //最大视在功率
                    .subAggregation(AggregationBuilders.max(APPARENT_POW_MAX_VALUE).field(APPARENT_POW))
                    //最小视在功率
                    .subAggregation(AggregationBuilders.min(APPARENT_POW_MIN_VALUE).field(APPARENT_POW))));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //查询结果
            SearchHits hits = searchResponse.getHits();
            LinkedList<PduHdaOutletRealtimeDo> resList = new LinkedList<>();

            for (SearchHit hit : hits.getHits()) {
                String str = hit.getSourceAsString();
                PduHdaOutletRealtimeDo realtimeDo = JsonUtils.parseObject(str, PduHdaOutletRealtimeDo.class);
                resList.add(realtimeDo);
            }


            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get("by_pdu");


            // 遍历terms聚合结果
            for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                // 获取按pduId分组
                Map<Object, PduHdaOutletBaseDo> dataMap = new HashMap<>();
                Terms byOutletAggregation = bucket.getAggregations().get("by_outlet");
                //获取按outlet_Id分组
                for (Terms.Bucket baseBucket : byOutletAggregation.getBuckets()) {
                    PduHdaOutletBaseDo baseDo = new PduHdaOutletBaseDo();
                    baseDo.setOutletId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {

                        if (field.equals(CUR_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setCurAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(CUR_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setCurMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(CUR_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setCurMinValue(((Double) min.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(ACTIVE_POW_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowMinValue(((Double) min.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(APPARENT_POW_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowMinValue(((Double) min.getValue()).floatValue());
                        }
                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(baseBucket.getKey(), baseDo);
                }
                result.put(bucket.getKey(), dataMap);
            }

            //获取时间
            Map<Integer, List<PduHdaOutletRealtimeDo>> realtimeDoMap = resList.stream().collect(Collectors.groupingBy(PduHdaOutletRealtimeDo::getPduId));
            realtimeDoMap.keySet().forEach(pduId -> {
                List<PduHdaOutletRealtimeDo> realtimeDos = realtimeDoMap.get(pduId);
                Map<Integer, List<PduHdaOutletRealtimeDo>> map = realtimeDos.stream().collect(Collectors.groupingBy(PduHdaOutletRealtimeDo::getOutletId));
                map.keySet().forEach(outletId -> {
                    List<PduHdaOutletRealtimeDo> list = map.get(outletId);

                    PduHdaOutletBaseDo fieldMap = result.get(Long.parseLong(String.valueOf(pduId))).get(Long.parseLong(String.valueOf(outletId)));

                    Map<Float, DateTime> curMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletRealtimeDo::getCur,PduHdaOutletRealtimeDo::getCreateTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> activePowMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletRealtimeDo::getActivePow,PduHdaOutletRealtimeDo::getCreateTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> apparentPowMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletRealtimeDo::getApparentPow,PduHdaOutletRealtimeDo::getCreateTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    fieldMap.setCurMinTime(curMap.get(fieldMap.getCurMinValue()));
                    fieldMap.setCurMaxTime(curMap.get(fieldMap.getCurMaxValue()));
                    fieldMap.setActivePowMaxTime(activePowMap.get(fieldMap.getActivePowMaxValue()));
                    fieldMap.setActivePowMinTime(activePowMap.get(fieldMap.getActivePowMinValue()));
                    fieldMap.setApparentPowMaxTime(apparentPowMap.get(fieldMap.getApparentPowMaxValue()));
                    fieldMap.setApparentPowMinTime(apparentPowMap.get(fieldMap.getActivePowMinValue()));

                    result.get(Long.parseLong(String.valueOf(pduId))).put(outletId, fieldMap);
                });
            });
            log.info("--------------------" + result);

            return result;

        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return result;
    }

    /**
     * 输出位历史数据统计(按天)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Object, Map<Object, PduHdaOutletBaseDo>> statisOutletDay(String startTime, String endTime) {
        Map<Object, Map<Object, PduHdaOutletBaseDo>> result = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(EsIndexEnum.PDU_HDA_OUTLET_HOUR.getIndex());
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime));

//            builder.query(QueryBuilders.matchAllQuery());
            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms("by_pdu")
                    .field("pdu_id");

            // 设置Avg指标聚合，按outlet_id分组
            TermsAggregationBuilder outletAggregationBuilder = AggregationBuilders.terms("by_outlet").field(OUTLET_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(outletAggregationBuilder
                    //统计平均电流
                    .subAggregation(AggregationBuilders.avg(CUR_AVG_VALUE).field(CUR_AVG_VALUE))
                    //最大电流
                    .subAggregation(AggregationBuilders.max(CUR_MAX_VALUE).field(CUR_MAX_VALUE))
                    //最小电流
                    .subAggregation(AggregationBuilders.min(CUR_MIN_VALUE).field(CUR_MIN_VALUE))
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW_AVG_VALUE))
                    //最大有功功率
                    .subAggregation(AggregationBuilders.max(ACTIVE_POW_MAX_VALUE).field(ACTIVE_POW_MAX_VALUE))
                    // 最小有功功率
                    .subAggregation(AggregationBuilders.min(ACTIVE_POW_MIN_VALUE).field(ACTIVE_POW_MIN_VALUE))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW_AVG_VALUE))
                    //最大视在功率
                    .subAggregation(AggregationBuilders.max(APPARENT_POW_MAX_VALUE).field(APPARENT_POW_MAX_VALUE))
                    //最小视在功率
                    .subAggregation(AggregationBuilders.min(APPARENT_POW_MIN_VALUE).field(APPARENT_POW_MIN_VALUE))));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //查询结果
            SearchHits hits = searchResponse.getHits();
            LinkedList<PduHdaOutletHourDo> resList = new LinkedList<>();

            for (SearchHit hit : hits.getHits()) {
                String str = hit.getSourceAsString();
                PduHdaOutletHourDo realtimeDo = JsonUtils.parseObject(str, PduHdaOutletHourDo.class);
                resList.add(realtimeDo);
            }


            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get("by_pdu");


            // 遍历terms聚合结果
            for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                // 获取按pduId分组
                Map<Object, PduHdaOutletBaseDo> dataMap = new HashMap<>();
                Terms byOutletAggregation = bucket.getAggregations().get("by_outlet");
                //获取按outlet_Id分组
                for (Terms.Bucket baseBucket : byOutletAggregation.getBuckets()) {
                    PduHdaOutletBaseDo baseDo = new PduHdaOutletBaseDo();
                    baseDo.setOutletId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {

                        if (field.equals(CUR_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setCurAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(CUR_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setCurMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(CUR_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setCurMinValue(((Double) min.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(ACTIVE_POW_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowMinValue(((Double) min.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(APPARENT_POW_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowMinValue(((Double) min.getValue()).floatValue());
                        }

                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(baseBucket.getKey(), baseDo);
                }
                result.put(bucket.getKey(), dataMap);
            }

            //获取时间
            Map<Integer, List<PduHdaOutletHourDo>> realtimeDoMap = resList.stream().collect(Collectors.groupingBy(PduHdaOutletHourDo::getPduId));
            realtimeDoMap.keySet().forEach(pduId -> {
                List<PduHdaOutletHourDo> realtimeDos = realtimeDoMap.get(pduId);
                Map<Integer, List<PduHdaOutletHourDo>> map = realtimeDos.stream().collect(Collectors.groupingBy(PduHdaOutletHourDo::getOutletId));
                map.keySet().forEach(outletId -> {
                    List<PduHdaOutletHourDo> list = map.get(outletId);

                    PduHdaOutletBaseDo fieldMap = result.get(Long.parseLong(String.valueOf(pduId))).get(Long.parseLong(String.valueOf(outletId)));

                    Map<Float, DateTime> curMaxMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletHourDo::getCurMaxValue,PduHdaOutletHourDo::getCurMaxTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    Map<Float, DateTime> curMinMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletHourDo::getCurMinValue,PduHdaOutletHourDo::getCurMinTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> activePowMaxMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletHourDo::getActivePowMaxValue,PduHdaOutletHourDo::getActivePowMaxTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    Map<Float, DateTime> activePowMinMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletHourDo::getActivePowMinValue,PduHdaOutletHourDo::getActivePowMinTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> apparentPowMaxMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletHourDo::getApparentPowMaxValue,PduHdaOutletHourDo::getApparentPowMaxTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    Map<Float, DateTime> apparentPowMinMap = list.stream().collect(Collectors
                            .toMap(PduHdaOutletHourDo::getApparentPowMinValue,PduHdaOutletHourDo::getApparentPowMinTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    fieldMap.setCurMinTime(curMinMap.get(fieldMap.getCurMinValue()));
                    fieldMap.setCurMaxTime(curMaxMap.get(fieldMap.getCurMaxValue()));
                    fieldMap.setActivePowMaxTime(activePowMaxMap.get(fieldMap.getActivePowMaxValue()));
                    fieldMap.setActivePowMinTime(activePowMinMap.get(fieldMap.getActivePowMinValue()));
                    fieldMap.setApparentPowMaxTime(apparentPowMaxMap.get(fieldMap.getApparentPowMaxValue()));
                    fieldMap.setApparentPowMinTime(apparentPowMinMap.get(fieldMap.getActivePowMinValue()));

                    result.get(Long.parseLong(String.valueOf(pduId))).put(outletId, fieldMap);

                });
            });
            log.info("--------------------" + result);

            return result;

        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return result;
    }

    /**
     * 判断两个值是否相等
     *
     * @param value
     * @param esValue
     */
    private boolean equalsValue(float value, float esValue) {
        BigDecimal doubleValue = BigDecimal.valueOf(value);
        BigDecimal floatValue = BigDecimal.valueOf(esValue);

        if (doubleValue.equals(floatValue)) {
            return true;
        }

        return false;

    }
}
