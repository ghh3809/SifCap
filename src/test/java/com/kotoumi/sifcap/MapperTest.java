package com.kotoumi.sifcap;

import com.alibaba.fastjson.JSON;
import com.kotoumi.sifcap.sif.Resolver;
import com.kotoumi.sifcap.utils.FileHelper;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

@Slf4j
public class MapperTest {

    @Test
    public void testUpdateUser() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/userInfo.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/userInfo.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.updateUserInfo(userId, JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testLivePlay() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/livePlay.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/livePlay.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.recordLiveInfo(userId, JSON.parseObject(requestLines.get(i)), JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testRlivePlay() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/rlivePlay.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/rlivePlay.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.recordLiveInfo(userId, JSON.parseObject(requestLines.get(i)), JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testDuelLivePlay() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/duelLivePlay.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/duelLivePlay.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.recordDuelLiveInfo(userId, JSON.parseObject(requestLines.get(i)), JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testProfileInfo() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/profileInfo.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/profileInfo.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.updateProfileInfo(userId, JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testUnitAll() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/unitAll.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/unitAll.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.updateUnitInfo(userId, JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testSecretBoxPon() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/secretBoxPon.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/secretBoxPon.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.recordSecretBoxInfo(userId, JSON.parseObject(responseLines.get(i)));
        }
    }

    @Test
    public void testUnitDeck() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/unitDeck.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/unitDeck.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.updateDeckInfo(userId, JSON.parseObject(requestLines.get(i)));
        }
    }

    @Test
    public void testUnitRemovableSkillInfo() {
        int userId = 6669728;
        List<String> requestLines = FileHelper.readLines("src/test/java/data/unitRemovableSkillInfo.request");
        List<String> responseLines = FileHelper.readLines("src/test/java/data/unitRemovableSkillInfo.response");
        TestCase.assertNotNull(requestLines);
        TestCase.assertNotNull(responseLines);
        for (int i = 0; i < Math.min(requestLines.size(), responseLines.size()); i ++) {
            Resolver.updateRemovableSkillInfo(userId, JSON.parseObject(responseLines.get(i)));
        }
    }

}
