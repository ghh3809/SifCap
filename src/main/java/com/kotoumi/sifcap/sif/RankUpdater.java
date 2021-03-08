package com.kotoumi.sifcap.sif;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kotoumi.sifcap.model.dao.Dao;
import com.kotoumi.sifcap.model.po.EventRank;
import com.kotoumi.sifcap.model.po.EventRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Map;

/**
 * @author guohaohao
 */
@Slf4j
public class RankUpdater implements Runnable {

    private static final int[] PLAYER_RANK = new int[] {120, 700, 2300};
    private static final int[] LIVE_RANK = new int[] {2300, 6900};
    private static final String PLAYER_URL = "http://localhost/main.php/ranking/eventPlayer";
    private static final String LIVE_URL = "http://localhost/main.php/ranking/eventLive";
    private static final String REQUEST_DATA_KEY = "request_data";

    @Override
    public void run() {

        try {
            // 当前正在进行的活动信息
            Integer eventId = Dao.getCurrentEventId();
            log.info("Begin execute scheduler, Current event id: {}", eventId);
            if (eventId == null) {
                return;
            }

            // 逐个查询排行榜
            String type = "pt";
            for (int rank : PLAYER_RANK) {
                List<EventRequest> eventRequestList = Dao.getEventRequests(eventId, type, rank);
                for (EventRequest eventRequest : eventRequestList) {
                    String requestHeaders = eventRequest.getRequestHeaders();
                    String requestData = eventRequest.getRequestData();
                    try {
                        if (updateRank(requestHeaders, requestData, eventId, type, rank)) {
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Update error");
                    }
                }
            }

            type = "live";
            for (int rank : LIVE_RANK) {
                List<EventRequest> eventRequestList = Dao.getEventRequests(eventId, type, rank);
                for (EventRequest eventRequest : eventRequestList) {
                    String requestHeaders = eventRequest.getRequestHeaders();
                    String requestData = eventRequest.getRequestData();
                    try {
                        if (updateRank(requestHeaders, requestData, eventId, type, rank)) {
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Update error");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Schedule error!", e);
        }

    }

    /**
     * 更新排行榜信息
     * @param requestHeaders 请求头
     * @param requestData 请求数据
     * @param type 类型
     * @return 是否更新成功
     */
    private boolean updateRank(String requestHeaders, String requestData, int eventId, String type, int rank) {

        // 准备请求
        JSONObject headersJson = JSON.parseObject(requestHeaders);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url;
        if ("pt".equals(type)) {
            url = PLAYER_URL;
        } else {
            url = LIVE_URL;
        }
        HttpPost httpPost = new HttpPost(url);
        HttpEntity reqEntity = MultipartEntityBuilder.create().addTextBody(REQUEST_DATA_KEY, requestData).build();
        log.info("Update request url: {}", url);
        log.info("Update request data: {}", requestData);
        httpPost.setEntity(reqEntity);
        for (Map.Entry<String, Object> entry : headersJson.entrySet()) {
            if (!"Content-Length".equals(entry.getKey()) && !"Content-Type".equals(entry.getKey())) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        String res;

        // 执行请求
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                log.error("Update error");
                return false;
            }
            res = EntityUtils.toString(response.getEntity());
            log.info("Update response: {}", res);
        } catch (Exception e) {
            log.error("Update error");
            return false;
        }

        // 结果转义
        JSONObject item;
        try {
            JSONObject jsonObject = JSON.parseObject(res);
            JSONArray items = jsonObject.getJSONObject("response_data").getJSONArray("items");
            item = items.getJSONObject(items.size() - 1);
        } catch (Exception e) {
            log.error("Update error");
            return false;
        }

        int finalRank = item.getInteger("rank");
        int score = item.getInteger("score");
        if (finalRank != rank) {
            return false;
        }

        // 导入数据库
        EventRank eventRank = new EventRank();
        eventRank.setEventId(eventId);
        eventRank.setType(type);
        eventRank.setRank(rank);
        eventRank.setScore(score);
        Dao.insertEventRank(eventRank);
        return true;

    }

}
