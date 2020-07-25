package com.kotoumi.sifcap.model.po;

import lombok.Data;

@Data
public class RemovableSkillEquipment {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 获取ID
     */
    private Long unitOwningUserId;
    /**
     * 宝石列表
     */
    private String unitRemovableSkillIdList;

}
