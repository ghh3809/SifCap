package com.kotoumi.sifcap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class RequestTest {

    @Test
    public void testRequest() throws IOException {

        String headers = "{\"User-Agent\":\"Love%20Live!/7.1 CFNetwork/1128.0.1 Darwin/19.6.0\",\"Accept-Encoding\":\"gzip, deflate\",\"X-Message-Code\":\"9d41baad91c5624d86d7599cd2b747adf45fff50\",\"Accept\":\"*/*\",\"Authorize\":\"consumerKey=lovelive_test&timeStamp=1615043359&version=1.1&token=e7kKtMbbsvcM5SOTjLVPdGpExy7q06RQoDZIr4lOO9e3t2nApfLR52WMpa9um5ev5BHXBCkYZ7HGJ6WWrLkJyQc&nonce=26\",\"Platform-Type\":\"1\",\"Region\":\"392\",\"Accept-Language\":\"zh-cn\",\"User-ID\":\"6669728\",\"OS-Version\":\"iPad11_3 iPad 13.6\",\"Application-ID\":\"626776655\",\"API-Model\":\"straightforward\",\"Content-Length\":\"259\",\"Debug\":\"1\",\"Content-Type\":\"multipart/form-data; boundary=wC98addtOZLgJNy48rp4\",\"OS\":\"iOS\",\"Connection\":\"close\",\"Host\":\"prod.game1.ll.sdo.com\",\"Client-Version\":\"71.3.5\",\"Bundle-Version\":\"7.1\",\"Time-Zone\":\"GMT+8\",\"X-BUNDLE-ID\":\"com.meiyu.lovelive\"}";
        String params = "{\"module\":\"ranking\",\"action\":\"eventPlayer\",\"buff\":0,\"timeStamp\":1615043359,\"mgd\":1,\"commandNum\":\"2623978090.1615043359.24\",\"event_id\":174,\"rank\":\"2300\"}";
        JSONObject headersJson = JSON.parseObject(headers);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://prod.game1.ll.sdo.com/main.php/ranking/eventPlayer");

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addTextBody("request_data", params)
                .build();
        log.info("Request_data: {}", params);
        httpPost.setEntity(reqEntity);
        for (Map.Entry<String, Object> entry : headersJson.entrySet()) {
            if (!entry.getKey().equals("Content-Length") &&
                    !entry.getKey().equals("Content-Type")) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        for (Header header : httpPost.getAllHeaders()) {
            log.info("{}: {}", header.getName(), header.getValue());
        }
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            log.info("code: {}", response.getStatusLine().getStatusCode());
            String res = EntityUtils.toString(response.getEntity());
            log.info(res);
        }
    }

}
