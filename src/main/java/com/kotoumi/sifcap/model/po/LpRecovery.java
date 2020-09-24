package com.kotoumi.sifcap.model.po;

import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class LpRecovery {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户uid
     */
    private Integer userId;
    /**
     * 恢复道具ID
     */
    private Integer itemId;
    /**
     * 使用数量
     */
    private Integer amount;
    /**
     * 当前最大LP
     */
    private Integer energyMax;
    /**
     * 回复时间
     */
    private String recoveryTime;

}
