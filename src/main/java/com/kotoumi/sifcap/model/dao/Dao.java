package com.kotoumi.sifcap.model.dao;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import com.kotoumi.sifcap.model.po.LivePlay;
import com.kotoumi.sifcap.model.po.User;

import java.io.IOException;
import java.io.Reader;

/**
 * @author guohaohao
 */
public class Dao {

    private static final SqlSessionFactory SQL_MAPPER;

    static {
        String resources = "mybatis-config.xml";
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
            session.insert("updateUser", user);
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

}
