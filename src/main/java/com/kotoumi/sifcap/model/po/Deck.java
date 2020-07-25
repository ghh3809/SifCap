package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class Deck {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户uid
     */
    private Integer userId;
    /**
     * 队伍编号
     */
    private Integer unitDeckId;
    /**
     * 是否是主力
     */
    private Integer mainFlag;
    /**
     * 队伍名称
     */
    private String deckName;
    /**
     * 队伍成员json
     */
    private String unitDeckDetailJson;

}
