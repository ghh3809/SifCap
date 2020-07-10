package com.kotoumi.sifcap.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guohaohao
 */
@Slf4j
public class FileHelper {


    /**
     * 将文件内容读取到变量中
     * @param file 文件路径
     */
    public static List<String> readLines(String file) {
        // 初始化
        List<String> lines = new ArrayList<>();
        BufferedReader br = null;

        try {
            Charset charset = StandardCharsets.UTF_8;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

            // 读文件
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            log.error("readLines error:{}", e.getMessage());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("close file error:{}", e.getMessage());
                }
            }
        }

        return lines;
    }

}
