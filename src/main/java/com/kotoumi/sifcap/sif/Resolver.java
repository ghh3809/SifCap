package com.kotoumi.sifcap.sif;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kotoumi.sifcap.model.dao.Dao;
import com.kotoumi.sifcap.model.po.Deck;
import com.kotoumi.sifcap.model.po.EffortBox;
import com.kotoumi.sifcap.model.po.RemovableSkillEquipment;
import com.kotoumi.sifcap.model.po.SecretBox;
import com.kotoumi.sifcap.model.po.Unit;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final Map<Integer, JSONObject> CACHED_LIVE = new HashMap<>();

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
            requestUrl = request.getRequestUrl();
            LoggerHelper.logInput("RequestUrl: " + requestUrl);

            requestJson = toJson(request);
            LoggerHelper.logInput("Request: " + requestJson.toJSONString());

            if (response == null) {
                log.error("HTTP response is null");
                return;
            }
            responseJson = toJson(response);
            LoggerHelper.logInput("Response: " + responseJson.toJSONString());

            if (StringUtils.isBlank(requestUrl)) {
                log.error("requestUrl is invalid: {}", requestUrl);
                return;
            }

            String userIdString = request.getHeaders().get("User-ID");
            if (StringUtils.isBlank(userIdString)) {
                log.error("userId is invalid: {}", userIdString);
                return;
            }
            userId = Integer.parseInt(userIdString);
            LoggerHelper.logInput("UserId: " + userId);

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
                requestUrl = request.getString("module") + "/" + request.getString("action");
                // result层有可能返回的是一个list
                Object resultObject = response.get("result");
                if (resultObject instanceof JSONObject) {
                    processSingle(userId, requestUrl, request, (JSONObject) resultObject);
                } else {
                    log.debug("Ignore non-object result");
                }
            }
        } else {
            if (!requestJson.containsKey(KEY_REQUEST_DATA)) {
                log.error("Request json not contains request_data");
                return;
            }
            if (!responseJson.containsKey(KEY_RESPONSE_DATA)) {
                log.error("Response json not contains response_data");
                return;
            }
            Object jsonObjectRequest = requestJson.get(KEY_REQUEST_DATA);
            Object jsonObjectResponse = responseJson.get(KEY_RESPONSE_DATA);
            // 有可能请求和返回的是list，这种情况不进行处理
            if (!(jsonObjectRequest instanceof JSONObject)) {
                log.debug("Ignore non-object request!");
                return;
            }
            if (jsonObjectResponse instanceof JSONObject) {
                processSingle(userId, requestUrl, (JSONObject) jsonObjectRequest, (JSONObject) jsonObjectResponse);
            } else {
                log.debug("Ignore non-object request or result");
                processSingle(userId, requestUrl, (JSONObject) jsonObjectRequest, new JSONObject());
            }
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
            // 用户基础信息
            case "user/userInfo":
                updateUserInfo(userId, responseJson);
                break;
            case "profile/profileInfo":
                updateProfileInfo(userId, responseJson);
                break;
            // 卡组信息
            case "unit/unitAll":
                updateUnitInfo(userId, responseJson);
                break;
            // 队伍信息
            case "unit/deck":
                updateDeckInfo(userId, requestJson);
                break;
            // 宝石信息
            case "unit/removableSkillInfo":
                updateRemovableSkillInfo(userId, responseJson);
                break;
            // SM活动
            case "battle/liveEnd":
                CACHED_LIVE.put(userId, requestJson);
                break;
            case "battle/endRoom":
                if (CACHED_LIVE.containsKey(userId)) {
                    recordLiveInfo(userId, CACHED_LIVE.remove(userId), responseJson);
                }
                break;
            // CF活动
            case "challenge/checkpoint":
            // 协力活动
            case "duty/liveEnd":
            // 散步拉力赛
            case "quest/questReward":
            // MF活动
            case "festival/liveReward":
            // 普通演唱会/随机演唱会
            case "live/reward":
            case "rlive/reward":
                recordLiveInfo(userId, requestJson, responseJson);
                break;
            // 招募
            case "secretbox/pon":
            case "secretbox/multi":
                recordSecretBoxInfo(userId, responseJson);
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
     * 更新用户详细信息
     * @param userId 用户ID
     * @param responseJson 响应JSON
     */
    public static void updateProfileInfo(int userId, JSONObject responseJson) {

        // 初始化
        if (!responseJson.containsKey("user_info")) {
            log.error("updateProfileInfo not found key: user_info");
            return;
        }
        User user = responseJson.getJSONObject("user_info").toJavaObject(User.class);
        user.setSettingAwardId(responseJson.getInteger("setting_award_id"));
        user.setSettingBackgroundId(responseJson.getInteger("setting_background_id"));
        user.setCenterUnitInfoJson(responseJson.getJSONObject("center_unit_info").toJSONString());
        user.setNaviUnitInfoJson(responseJson.getJSONObject("navi_unit_info").toJSONString());

        // 入库
        if (Dao.findUser(userId) == null) {
            Dao.insertUser(user);
        } else {
            Dao.updateUser(user);
        }
    }

    /**
     * 更新用户卡组信息
     * @param userId 用户ID
     * @param responseJson 响应JSON
     */
    public static void updateUnitInfo(int userId, JSONObject responseJson) {

        // 初始化
        List<Unit> unitList = new ArrayList<>();
        updatePartUnitInfo(userId, responseJson, unitList, "active");
        updatePartUnitInfo(userId, responseJson, unitList, "waiting");

        // 入库
        Dao.batchDeleteUnit(userId);
        Dao.batchAddUnit(unitList);

    }

    /**
     * 更新用户队伍信息
     * @param userId 用户ID
     * @param requestJson 请求JSON
     */
    public static void updateDeckInfo(int userId, JSONObject requestJson) {

        // 初始化
        if (!requestJson.containsKey("unit_deck_list")) {
            log.error("updateDeckInfo not found key: unit_deck_list");
            return;
        }

        // 更新队伍信息
        List<Deck> deckList = new ArrayList<>(9);
        JSONArray unitDeckList = requestJson.getJSONArray("unit_deck_list");
        for (int i = 0; i < unitDeckList.size(); i ++) {
            JSONObject unitDeck = unitDeckList.getJSONObject(i);
            Deck deck = unitDeck.toJavaObject(Deck.class);
            deck.setUserId(userId);
            deck.setUnitDeckDetailJson(unitDeck.getJSONArray("unit_deck_detail").toJSONString());
            deckList.add(deck);
        }

        // 入库
        Dao.batchDeleteDeck(userId);
        Dao.batchAddDeck(deckList);

    }

    /**
     * 更新用户宝石信息
     * @param userId 用户ID
     * @param responseJson 响应JSON
     */
    public static void updateRemovableSkillInfo(int userId, JSONObject responseJson) {

        List<RemovableSkillEquipment> removableSkillEquipmentList = new ArrayList<>();

        // 更新宝石使用信息
        if (responseJson.containsKey("equipment_info")) {
            JSONObject equipmentInfo = responseJson.getJSONObject("equipment_info");
            for (Object value : equipmentInfo.values()) {
                if (value instanceof JSONObject) {
                    Long unitOwningUserId = ((JSONObject) value).getLong("unit_owning_user_id");
                    JSONArray details = ((JSONObject) value).getJSONArray("detail");
                    List<Integer> unitRemovableSkillIdList = new ArrayList<>(details.size());
                    for (int i = 0; i < details.size(); i ++) {
                        JSONObject detail = details.getJSONObject(i);
                        unitRemovableSkillIdList.add(detail.getInteger("unit_removable_skill_id"));
                    }
                    RemovableSkillEquipment removableSkillEquipment = new RemovableSkillEquipment();
                    removableSkillEquipment.setUserId(userId);
                    removableSkillEquipment.setUnitOwningUserId(unitOwningUserId);
                    removableSkillEquipment.setUnitRemovableSkillIdList(JSON.toJSONString(unitRemovableSkillIdList));
                    removableSkillEquipmentList.add(removableSkillEquipment);
                }
            }
        }

        // 入库
        Dao.batchDeleteRemovableSkillEquipment(userId);
        Dao.batchAddRemovableSkillEquipment(removableSkillEquipmentList);

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
        JSONArray unitListJson = null;
        List<EffortBox> effortBoxList = new ArrayList<>();
        livePlay.setUserId(userId);
        try {
            JSONArray liveInfo;
            JSONObject baseRewardInfo;

            // 根据场景不同，需要提取不同的live信息和奖励信息
            if (responseJson.containsKey("challenge_result")) {
                JSONObject challengeResult = responseJson.getJSONObject("challenge_result");
                liveInfo = challengeResult.getJSONArray("live_info");
                baseRewardInfo = challengeResult.getJSONObject("reward_info");
            } else {
                liveInfo = responseJson.getJSONArray("live_info");
                baseRewardInfo = responseJson.getJSONObject("base_reward_info");
            }

            // 从live信息和奖励信息中提取数据
            if (liveInfo != null && !liveInfo.isEmpty()) {
                if (liveInfo.size() > 1) {
                    log.info("Ignore multi live info: {}", liveInfo.size());
                }
                JSONObject live = liveInfo.getJSONObject(0);
                livePlay.setLiveDifficultyId(live.getInteger("live_difficulty_id"));
                livePlay.setIsRandom(live.getBoolean("is_random"));
                livePlay.setAcFlag(live.getInteger("ac_flag"));
                livePlay.setSwingFlag(live.getInteger("swing_flag"));
            }
            if (baseRewardInfo != null) {
                livePlay.setExpCnt(baseRewardInfo.getInteger("player_exp"));
                livePlay.setGameCoinCnt(baseRewardInfo.getInteger("game_coin"));
                livePlay.setSocialPointCnt(baseRewardInfo.getInteger("social_point"));
            }

            // 提取打歌队伍信息
            if (responseJson.containsKey("unit_list")) {
                unitListJson = responseJson.getJSONArray("unit_list");
                livePlay.setUnitListJson(unitListJson.toJSONString());
            }

            // 提取打歌时间
            if (responseJson.containsKey("server_timestamp")) {
                livePlay.setPlayTime(SIMPLE_DATE_FORMAT.format(new Date(responseJson.getLong("server_timestamp") * 1000)));
            }

            // 提取奖励箱子信息
            if (responseJson.containsKey("effort_point")) {
                JSONArray effortPointJsonList = responseJson.getJSONArray("effort_point");
                for (int i = 0; i < effortPointJsonList.size(); i ++) {
                    JSONObject effortPointJson = effortPointJsonList.getJSONObject(i);
                    if (effortPointJson.containsKey("rewards")) {
                        JSONArray rewardsJson = effortPointJson.getJSONArray("rewards");
                        if (rewardsJson.size() > 0) {
                            EffortBox effortBox = effortPointJson.toJavaObject(EffortBox.class);
                            effortBox.setUserId(userId);
                            effortBox.setRewardsJson(rewardsJson.toJSONString());
                            effortBox.setOpenTime(livePlay.getPlayTime());
                            effortBoxList.add(effortBox);
                        }
                    }
                }
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

        // 尝试入库成员信息
        if (unitListJson != null) {
            // 获取所有成员的unitOwningUserId
            List<Unit> unitList = new ArrayList<>(9);
            for (int i = 0; i < unitListJson.size(); i++) {
                JSONObject unitJson = unitListJson.getJSONObject(i);
                Unit unit = unitJson.toJavaObject(Unit.class);
                unit.setUserId(userId);
                unit.setStatus("deck");
                unit.setRank(unit.getIsRankMax() ? 2 : 1);
                unit.setDisplayRank(unit.getRank());
                unit.setUnitRemovableSkillCapacity(4);
                unitList.add(unit);
            }

            // 数据更新入库
            Dao.batchDeleteSelectUnit(userId, unitList);
            Dao.batchAddUnit(unitList);

        }

        // 尝试入库箱子信息
        if (!effortBoxList.isEmpty()) {
            Dao.batchAddEffort(effortBoxList);
        }

    }

    /**
     * 记录招募信息
     * @param userId 用户ID
     * @param responseJson 响应JSON
     */
    public static void recordSecretBoxInfo(int userId, JSONObject responseJson) {

        // 检查必须项
        if (!responseJson.containsKey("secret_box_info") || !responseJson.containsKey("secret_box_items")
                || !responseJson.containsKey("server_timestamp")) {
            log.error("Invalid secret box response");
        }

        List<SecretBox> secretBoxList = new ArrayList<>();

        JSONObject secretBoxInfo = responseJson.getJSONObject("secret_box_info");
        Integer secretBoxId = secretBoxInfo.getInteger("secret_box_id");
        String name = secretBoxInfo.getString("name");
        String ponTime = SIMPLE_DATE_FORMAT.format(new Date(responseJson.getLong("server_timestamp") * 1000));

        JSONObject secretBoxItems = responseJson.getJSONObject("secret_box_items");
        if (secretBoxItems.containsKey("unit")) {
            JSONArray unitList = secretBoxItems.getJSONArray("unit");
            for (int i = 0; i < unitList.size(); i ++) {
                JSONObject unit = unitList.getJSONObject(i);
                SecretBox secretBox = new SecretBox();
                secretBox.setUserId(userId);
                secretBox.setSecretBoxId(secretBoxId);
                secretBox.setName(name);
                secretBox.setUnitId(unit.getInteger("unit_id"));
                secretBox.setRank(unit.getInteger("rank"));
                secretBox.setRarity(unit.getInteger("unit_rarity_id"));
                secretBox.setPonTime(ponTime);
                secretBoxList.add(secretBox);
            }
        }

        // 入库
        Dao.batchInsertSecretBox(secretBoxList);

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

    /**
     * 更新部分用户信息
     * @param userId 用户ID
     * @param responseJson 返回信息
     * @param unitList 待加入的成员列表
     * @param key 需加入的key
     */
    private static void updatePartUnitInfo(int userId, JSONObject responseJson, List<Unit> unitList, String key) {
        if (responseJson.containsKey(key)) {
            JSONArray unitListJson = responseJson.getJSONArray(key);
            for (int i = 0; i < unitListJson.size(); i ++) {
                Unit unit = unitListJson.getJSONObject(i).toJavaObject(Unit.class);
                unit.setUserId(userId);
                unit.setStatus(key);
                unitList.add(unit);
            }
        }
    }

}
