package com.kotoumi.sifcap.model.dao;

import com.kotoumi.sifcap.model.po.Deck;
import com.kotoumi.sifcap.model.po.DuelLiveBox;
import com.kotoumi.sifcap.model.po.EffortBox;
import com.kotoumi.sifcap.model.po.LpRecovery;
import com.kotoumi.sifcap.model.po.RemovableSkillEquipment;
import com.kotoumi.sifcap.model.po.SecretBox;
import com.kotoumi.sifcap.model.po.Unit;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import com.kotoumi.sifcap.model.po.LivePlay;
import com.kotoumi.sifcap.model.po.User;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guohaohao
 */
public class Dao {

    private static final SqlSessionFactory SQL_MAPPER;

    static {
        // 这里打开调试开关
        boolean isDev = false;
        String resources;
        if (isDev) {
            resources = "mybatis-config-dev.xml";
        } else {
            resources = "mybatis-config.xml";
        }

        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(resources);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQL_MAPPER = new SqlSessionFactoryBuilder().build(reader);
    }

    /**
     * 插入用户信息
     * @param user 用户信息
     */
    public static void insertUser(User user) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("insertUser", user);
            session.commit();
        }
    }

    /**
     * 根据userId寻找用户
     * @param userId uid
     * @return
     */
    public static User findUser(int userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            return session.selectOne("findUser", userId);
        }
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     */
    public static void updateUser(User user) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.update("updateUser", user);
            session.commit();
        }
    }

    /**
     * 插入演唱会记录
     * @param livePlay 演唱会记录
     */
    public static void insertLivePlay(LivePlay livePlay) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("insertLivePlay", livePlay);
            session.commit();
        }
    }

    /**
     * 批量插入卡组信息
     * @param list 卡组列表
     */
    public static void batchAddUnit(List<Unit> list) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("batchAddUnit", list);
            session.commit();
        }
    }

    /**
     * 批量删除卡组信息
     */
    public static void batchDeleteUnit(int userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.delete("batchDeleteUnit", userId);
            session.commit();
        }
    }

    /**
     * 批量删除卡组信息
     */
    public static Unit checkValidUnit(int userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            return session.selectOne("checkValidUnit", userId);
        }
    }

    /**
     * 批量删除卡组信息
     */
    public static void batchDeleteSelectUnit(int userId, List<Unit> unitList) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("unitList", unitList);
            session.delete("batchDeleteSelectUnit", params);
            session.commit();
        }
    }

    /**
     * 插入招募记录
     * @param list 招募记录
     */
    public static void batchInsertSecretBox(List<SecretBox> list) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("batchInsertSecretBox", list);
            session.commit();
        }
    }

    /**
     * 批量插入队伍信息
     * @param list 队伍列表
     */
    public static void batchAddDeck(List<Deck> list) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("batchAddDeck", list);
            session.commit();
        }
    }

    /**
     * 批量删除队伍信息
     */
    public static void batchDeleteDeck(int userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.delete("batchDeleteDeck", userId);
            session.commit();
        }
    }

    /**
     * 批量插入队伍信息
     * @param list 队伍列表
     */
    public static void batchAddRemovableSkillEquipment(List<RemovableSkillEquipment> list) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("batchAddRemovableSkillEquipment", list);
            session.commit();
        }
    }

    /**
     * 批量删除队伍信息
     */
    public static void batchDeleteRemovableSkillEquipment(int userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.delete("batchDeleteRemovableSkillEquipment", userId);
            session.commit();
        }
    }

    /**
     * 批量插入队伍信息
     * @param list 箱子列表
     */
    public static void batchAddEffort(List<EffortBox> list) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("batchAddEffort", list);
            session.commit();
        }
    }

    /**
     * 插入百人协力奖励记录
     * @param duelLiveBox 百人协力奖励记录
     */
    public static void insertDuelLiveBox(DuelLiveBox duelLiveBox) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("insertDuelLiveBox", duelLiveBox);
            session.commit();
        }
    }

    /**
     * 插入LP回复记录
     * @param lpRecovery LP回复记录
     */
    public static void insertLpRecovery(LpRecovery lpRecovery) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("insertLpRecovery", lpRecovery);
            session.commit();
        }
    }

}
