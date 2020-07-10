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

}
