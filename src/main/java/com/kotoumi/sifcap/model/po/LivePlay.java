package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class LivePlay {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * uid
     */
    private Integer userId;
    /**
     * 歌曲难度ID
     */
    private Integer liveDifficultyId;
    /**
     * 是否是随机歌曲
     */
    private Boolean isRandom;
    /**
     * 是否是AC歌曲
     */
    private Integer acFlag;
    /**
     * 是否是滑键歌曲
     */
    private Integer swingFlag;
    /**
     * perfect数
     */
    private Integer perfectCnt;
    /**
     * great数
     */
    private Integer greatCnt;
    /**
     * good数
     */
    private Integer goodCnt;
    /**
     * bad数
     */
    private Integer badCnt;
    /**
     * miss数
     */
    private Integer missCnt;
    /**
     * 连击数
     */
    private Integer maxCombo;
    /**
     * smile分数
     */
    private Integer scoreSmile;
    /**
     * cute分数
     */
    private Integer scoreCute;
    /**
     * cool分数
     */
    private Integer scoreCool;
    /**
     * 活动ID
     */
    private Integer eventId;
    /**
     * 获得绊点
     */
    private Integer loveCnt;
    /**
     * 获得EXP
     */
    private Integer expCnt;
    /**
     * 获得金币
     */
    private Integer gameCoinCnt;
    /**
     * 获得友情点
     */
    private Integer socialPointCnt;
    /**
     * 使用队伍
     */
    private String unitListJson;
    /**
     * 打歌时间
     */
    private String playTime;

}
