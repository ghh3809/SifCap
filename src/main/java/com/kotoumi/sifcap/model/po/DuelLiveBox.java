package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class DuelLiveBox {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户uid
     */
    private Integer userId;
    /**
     * 分数rank
     */
    private Integer scoreRank;
    /**
     * 连击rank
     */
    private Integer comboRank;
    /**
     * 完成奖励JSON
     */
    private String liveClearJson;
    /**
     * 分数奖励JSON
     */
    private String liveRankJson;
    /**
     * 连击奖励JSON
     */
    private String liveComboJson;
    /**
     * 开箱时间
     */
    private String openTime;

}
