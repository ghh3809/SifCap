package com.kotoumi.sifcap.model.dao;

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
import java.util.List;

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
     * 插入招募记录
     * @param list 招募记录
     */
    public static void batchInsertSecretBox(List<SecretBox> list) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("batchInsertSecretBox", list);
            session.commit();
        }
    }

}
