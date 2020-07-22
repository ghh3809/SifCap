package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class SecretBox {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 招募id
     */
    private Integer secretBoxId;
    /**
     * 招募名称
     */
    private String name;
    /**
     * 成员id
     */
    private Integer unitId;
    /**
     * 当前rank
     */
    private Integer rank;
    /**
     * 成员稀有度
     */
    private Integer rarity;
    /**
     * 招募时间
     */
    private String ponTime;

}
