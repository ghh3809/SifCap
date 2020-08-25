package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class EffortBox {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户uid
     */
    private Integer userId;
    /**
     * 箱子类型1-5，按顺序对应10/40/100/200/400箱子
     */
    private Integer liveEffortPointBoxSpecId;
    /**
     * 蛋的类型（箱子没有）
     */
    private Integer limitedEffortEventId;
    /**
     * 容量
     */
    private Integer capacity;
    /**
     * 奖励JSON，要求不为空
     */
    private String rewardsJson;
    /**
     * 开箱时间
     */
    private String openTime;

}
