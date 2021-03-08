package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class EventRank {

    /**
     * 自增ID
     */
    private Long id;
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
     * 排名对应的分数
     */
    private Integer score;

}
