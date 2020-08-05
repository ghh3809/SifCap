package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class Unit {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 成员状态：active/waiting/deck
     */
    private String status;
    /**
     * 当前绊点
     */
    private Integer love;
    /**
     * 最大绊点
     */
    private Integer maxLove;
    /**
     * 技能等级是否最大
     */
    private Boolean isSkillLevelMax;
    /**
     * 成员等级
     */
    private Integer level;
    /**
     * 获取时间
     */
    private String insertDate;
    /**
     * 技能等级
     */
    private Integer unitSkillLevel;
    /**
     * 技能经验
     */
    private Integer unitSkillExp;
    /**
     * 最大hp
     */
    private Integer maxHp;
    /**
     * 显示rank
     */
    private Integer displayRank;
    /**
     * 宝石容量
     */
    private Integer unitRemovableSkillCapacity;
    /**
     * 获取id
     */
    private Long unitOwningUserId;
    /**
     * 等级是否最大
     */
    private Boolean isLevelMax;
    /**
     * 最大觉醒rank
     */
    private Integer maxRank;
    /**
     * 最大等级
     */
    private Integer maxLevel;
    /**
     * 是否满绊
     */
    private Boolean isLoveMax;
    /**
     * 是否最大宝石容量
     */
    private Boolean isRemovableSkillCapacityMax;
    /**
     * 当前觉醒rank
     */
    private Integer rank;
    /**
     * 下一级exp
     */
    private Integer nextExp;
    /**
     * 当前exp
     */
    private Integer exp;
    /**
     * 成员ID
     */
    private Integer unitId;
    /**
     * 是否锁定（存疑）
     */
    private Boolean favoriteFlag;
    /**
     * 是否最大觉醒等级
     */
    private Boolean isRankMax;

}
