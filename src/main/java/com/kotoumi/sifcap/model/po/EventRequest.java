package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class EventRequest {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * uid
     */
    private Integer userId;
    /**
     * 活动ID
     */
    private Integer eventId;
    /**
     * 档线类型，pt（活动点数榜）/live（歌榜）
     */
    private String type;
    /**
     * 对应排名
     */
    private Integer rank;
    /**
     * 请求头
     */
    private String requestHeaders;
    /**
     * 请求request_data数据
     */
    private String requestData;

}
