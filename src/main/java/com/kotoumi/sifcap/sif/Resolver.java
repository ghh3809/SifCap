package com.kotoumi.sifcap.sif;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kotoumi.sifcap.model.dao.Dao;
import com.kotoumi.sifcap.utils.LoggerHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.kotoumi.sifcap.model.HttpData;
import com.kotoumi.sifcap.model.RequestResponsePair;
import com.kotoumi.sifcap.model.po.LivePlay;
import com.kotoumi.sifcap.model.po.User;
import com.kotoumi.sifcap.utils.CompressHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guohaohao
 */
@Slf4j
public class Resolver {

    private static final String ENCODING_GZIP = "gzip";
    private static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String LINE_END = "\r\n";
    private static final String URL_PREFIX = "/main.php/";
    private static final String URL_API = "api";
    private static final String KEY_REQUEST_DATA = "request_data";
    private static final String KEY_RESPONSE_DATA = "response_data";
    private static final Pattern NAME_PATTERN = Pattern.compile("^Content-Disposition: form-data;.*name=\"([^)]*)\".*$");
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 对请求-回应对进行解析
     * @param requestResponsePair 需要解析的请求-回应对
     */
    public static void resolve(RequestResponsePair requestResponsePair) {

        int userId;
        String requestUrl;
        JSONObject requestJson;
        JSONObject responseJson;

        try {
            // 数据提取和校验
            HttpData request = requestResponsePair.getRequest();
            HttpData response = requestResponsePair.getResponse();
            if (request == null) {
                log.error("HTTP request is null");
                return;
            }
            if (response == null) {
                log.error("HTTP response is null");
                return;
            }
            String userIdString = request.getHeaders().get("User-ID");
            if (StringUtils.isBlank(userIdString)) {
                log.error("userId is invalid: {}", userIdString);
                return;
            }
            userId = Integer.parseInt(userIdString);
            requestUrl = request.getRequestUrl();
            requestJson = toJson(request);
            responseJson = toJson(response);
            if (StringUtils.isBlank(requestUrl)) {
                log.error("requestUrl is invalid: {}", requestUrl);
                return;
            }
            LoggerHelper.logInput("UserId: " + userId);
            LoggerHelper.logInput("RequestUrl: " + requestUrl);
            LoggerHelper.logInput("Request: " + requestJson.toJSONString());
            LoggerHelper.logInput("Response: " + responseJson.toJSONString());
        } catch (Exception e) {
            log.error("Resolve requestResponsePair failed");
            return;
        }

        try {
            process(userId, requestUrl, requestJson, responseJson);
        } catch (Exception e) {
            log.error("Process requestResponsePair failed");
            return;
        }

    }

    /**
     * 对解析后的数据进行处理
     * @param userId 用户ID
     * @param requestUrl 请求地址
     * @param requestJson 请求JSON
     * @param responseJson 响应JSON
     */
    private static void process(int userId, String requestUrl, JSONObject requestJson, JSONObject responseJson) {

        if (!requestUrl.startsWith(URL_PREFIX)) {
            log.error("Could not resolve url: {}", requestUrl);
            return;
        }
        requestUrl = requestUrl.substring(10);

        // api接口，需要进行接口拆解，逐个接口进行解析
        if (URL_API.equals(requestUrl)) {
            if (!requestJson.containsKey(KEY_REQUEST_DATA)) {
                log.error("Request json not contains request_data");
                return;
            }
            if (!responseJson.containsKey(KEY_RESPONSE_DATA)) {
                log.error("Response json not contains response_data");
                return;
            }
            JSONArray jsonArrayRequest = requestJson.getJSONArray(KEY_REQUEST_DATA);
            JSONArray jsonArrayResponse = responseJson.getJSONArray(KEY_RESPONSE_DATA);
            if (jsonArrayRequest.size() != jsonArrayResponse.size()) {
                log.error("Request and response size not equal: {} / {}",
                        jsonArrayRequest.size(), jsonArrayResponse.size());
                return;
            }
            for (int i = 0; i < jsonArrayRequest.size(); i ++) {
                JSONObject request = jsonArrayRequest.getJSONObject(i);
                JSONObject response = jsonArrayResponse.getJSONObject(i);
                if (!request.containsKey("module") || !request.containsKey("action")) {
                    log.error("Request not contains key module or action");
                    continue;
                }
                if (!response.containsKey("result")) {
                    log.error("Response not contains key result");
                    continue;
                }
                response = response.getJSONObject("result");
                requestUrl = request.getString("module") + "/" + request.getString("action");
                processSingle(userId, requestUrl, request, response);
            }
        } else {
            processSingle(userId, requestUrl, requestJson.getJSONObject("request_data"),
                    responseJson.getJSONObject("response_data"));
        }

    }

    /**
     * 单独处理一个接口的数据
     * @param userId 用户ID
     * @param requestUrl 请求地址
     * @param requestJson 请求JSON
     * @param responseJson 响应JSON
     */
    private static void processSingle(int userId, String requestUrl, JSONObject requestJson, JSONObject responseJson) {
        log.info("userId: {}", userId);
        log.info("requestUrl: {}", requestUrl);
        log.info("requestJson: {}", requestJson);
        log.info("responseJson: {}", responseJson);
        switch (requestUrl) {
            case "user/userInfo":
                updateUserInfo(userId, responseJson);
                break;
            // SM活动
            case "battle/liveEnd":
            // CF活动
            case "challenge/checkpoint":
            // 协力活动
            case "duty/liveEnd":
            // 散步拉力赛
            case "quest/questReward":
            // MF活动
            case "festival/liveReward":
            // 普通演唱会
            case "live/reward":
                recordLiveInfo(userId, requestJson, responseJson);
                break;
            default:
                log.info("Ignore url: {}", requestUrl);
                break;
        }
    }

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param responseJson 响应JSON
     */
    public static void updateUserInfo(int userId, JSONObject responseJson) {

        // 初始化
        if (!responseJson.containsKey("user")) {
            log.error("updateUserInfo not found key: user");
            return;
        }
        User user = responseJson.getJSONObject("user").toJavaObject(User.class);

        // 更新生日
        try {
            if (responseJson.containsKey("birth")) {
                JSONObject birth = responseJson.getJSONObject("birth");
                if (birth.containsKey("birth_month")) {
                    String birthMonth = birth.getString("birth_month");
                    if (birthMonth != null) {
                        user.setBirthMonth(Integer.parseInt(birthMonth));
                    }
                }
                if (birth.containsKey("birth_day")) {
                    String birthDay = birth.getString("birth_day");
                    if (birthDay != null) {
                        user.setBirthDay(Integer.parseInt(birthDay));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Resolve birth failed!");
        }

        // 入库
        if (Dao.findUser(userId) == null) {
            Dao.insertUser(user);
        } else {
            Dao.updateUser(user);
        }
    }

    /**
     * 记录演唱会信息
     * @param userId 用户ID
     * @param requestJson 请求JSON
     * @param responseJson 响应JSON
     */
    public static void recordLiveInfo(int userId, JSONObject requestJson, JSONObject responseJson) {

        // 初始化
        LivePlay livePlay = requestJson.toJavaObject(LivePlay.class);

        // 更新其他信息
        livePlay.setUserId(userId);
        try {
            if (responseJson.containsKey("live_info")) {
                JSONArray liveInfo = responseJson.getJSONArray("live_info");
                if (!liveInfo.isEmpty()) {
                    if (liveInfo.size() > 1) {
                        log.info("Ignore multi live info: {}", liveInfo.size());
                    }
                    JSONObject live = liveInfo.getJSONObject(0);
                    livePlay.setIsRandom(live.getBoolean("is_random"));
                    livePlay.setAcFlag(live.getInteger("ac_flag"));
                    livePlay.setSwingFlag(live.getInteger("swing_flag"));
                }
            }
            if (responseJson.containsKey("base_reward_info")) {
                JSONObject baseRewardInfo = responseJson.getJSONObject("base_reward_info");
                livePlay.setExpCnt(baseRewardInfo.getInteger("player_exp"));
                livePlay.setGameCoinCnt(baseRewardInfo.getInteger("game_coin"));
                livePlay.setSocialPointCnt(baseRewardInfo.getInteger("social_point"));
            }
            if (responseJson.containsKey("unit_list")) {
                livePlay.setUnitList(responseJson.getJSONArray("unit_list").toJSONString());
            }
            if (responseJson.containsKey("server_timestamp")) {
                livePlay.setPlayTime(SIMPLE_DATE_FORMAT.format(new Date(responseJson.getLong("server_timestamp") * 1000)));
            }
        } catch (Exception e) {
            log.error("Resolve live play info failed");
        }

        // 入库
        Dao.insertLivePlay(livePlay);

        // 尝试更新用户信息
        if (responseJson.containsKey("after_user_info")) {
            User user = responseJson.getJSONObject("after_user_info").toJavaObject(User.class);
            user.setUserId(userId);
            Dao.updateUser(user);
        }

    }

    /**
     * 将HttpData中的payload转化为json数据
     * @param httpData httpData
     * @return json数据
     */
    private static JSONObject toJson(HttpData httpData) {
        // 获取原始的payload
        byte[] payload = ArrayUtils.toPrimitive(httpData.getPayloadList().toArray(new Byte[0]));

        // gzip解压
        if (ENCODING_GZIP.equals(httpData.getContentEncoding())) {
            payload = CompressHelper.gzipDecompression(payload);
            if (payload == null) {
                return new JSONObject();
            }
        }

        // form-data格式转义
        if (httpData.getContentType().contains(CONTENT_TYPE_FORM_DATA)) {
            JSONObject jsonObject = new JSONObject();
            String[] lines = new String(payload).split(LINE_END);
            String separator = lines[0];
            int stage = -1;
            String key = null;
            for (String line : lines) {
                if (line.contains(separator)) {
                    stage = 0;
                } else if (stage == 0) {
                    Matcher matcher = NAME_PATTERN.matcher(line);
                    if (matcher.find()) {
                        stage = 1;
                        key = matcher.group(1);
                    }
                } else if (stage == 1) {
                    if (StringUtils.isEmpty(line)) {
                        stage = 2;
                    }
                } else if (stage == 2) {
                    try {
                        // 尝试以JSON模式进行解析，若失败则当做字符串
                        JSONObject jsonObjectValue = JSON.parseObject(line);
                        jsonObject.put(key, jsonObjectValue);
                    } catch (Exception e) {
                        try {
                            JSONArray jsonArrayValue = JSON.parseArray(line);
                            jsonObject.put(key, jsonArrayValue);
                        } catch (Exception e1) {
                            jsonObject.put(key, line);
                        }
                    }
                }
            }
            return jsonObject;
        } else if (httpData.getContentType().contains(CONTENT_TYPE_JSON)) {
            return JSON.parseObject(new String(payload));
        } else {
            log.info("Could not resolve content type: {}", httpData.getContentType());
            return new JSONObject();
        }
    }

}
