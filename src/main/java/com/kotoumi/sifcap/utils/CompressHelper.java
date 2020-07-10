package com.kotoumi.sifcap.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author guohaohao
 */
@Slf4j
public class CompressHelper {

    /**
     * gzip解压缩数组
     */
    public static byte[] gzipDecompression(byte[] data) {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int read;
            byte[] buffer = new byte[1024];
            while ((read = gis.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            baos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("gzipDecompression error: {}", e.fillInStackTrace().toString());
        }
        return null;
    }

}
