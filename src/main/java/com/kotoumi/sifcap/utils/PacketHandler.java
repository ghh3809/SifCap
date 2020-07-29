package com.kotoumi.sifcap.utils;

import com.kotoumi.sifcap.model.HttpData;
import com.kotoumi.sifcap.model.RequestResponsePair;
import com.kotoumi.sifcap.sif.Resolver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author guohaohao
 */
@Slf4j
public class PacketHandler<T> implements PcapPacketHandler<T> {

    private static final String LOCAL_IP = ConfigHelper.getProperties("ip");
    private static final String SDO_HOST = ConfigHelper.getProperties("host");
    private static final Set<String> BLACK_URL_SET = new HashSet<>();
    private static final Set<String> BLACK_IP_SET = new HashSet<>();
    private static final Map<Integer, RequestResponsePair> PAIR_MAP = new HashMap<>();
    private static final Map<Integer, Long> IGNORE_PORT = new HashMap<>();
    private static long lastCheckTime = System.currentTimeMillis();

    static {
        Collections.addAll(BLACK_IP_SET, ConfigHelper.getProperties("black_ip").split(","));
        Collections.addAll(BLACK_URL_SET, ConfigHelper.getProperties("black_url").split(","));
    }

    @SneakyThrows
    @Override
    public void nextPacket(PcapPacket pack, T user) {

        Ip4 ip4 = new Ip4();
        Tcp tcp = new Tcp();
        Http http = new Http();

        // 检查是否存在过期请求，及时清理
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime > 60000) {
            Iterator<Map.Entry<Integer, RequestResponsePair>> it = PAIR_MAP.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, RequestResponsePair> entry = it.next();
                if (currentTime - entry.getValue().getTimestamp() > 30000) {
                    log.info("Clear timeout port: {}", entry.getKey());
                    it.remove();
                }
            }
            Iterator<Map.Entry<Integer, Long>> it2 = IGNORE_PORT.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<Integer, Long> entry = it2.next();
                if (currentTime - entry.getValue() > 30000) {
                    log.info("Clear timeout port: {}", entry.getKey());
                    it2.remove();
                }
            }
            lastCheckTime = currentTime;
        }

        try {
            // 拦截所有tcp信息，并且要求其负载不为空
            if (pack.hasHeader(ip4) && pack.hasHeader(tcp) && tcp.getPayload().length > 0) {

                // 获取IP
                String dstIp = FormatUtils.ip(ip4.destination());
                String srcIp = FormatUtils.ip(ip4.source());
                long seq = tcp.seq();
                long ack = tcp.ack();
                int source = tcp.source();
                int destination = tcp.destination();

                // 过滤已忽略的数据
                if (IGNORE_PORT.containsKey(source) || IGNORE_PORT.containsKey(destination)) {
                    log.debug("Ignore packet");
                    return;
                }

                // 过滤黑名单ip
                if (BLACK_IP_SET.contains(srcIp) || BLACK_IP_SET.contains(dstIp)) {
                    log.debug("Ignore packet");
                    return;
                }

                // 不关心服务器与客户端之间的数据流通
                if (srcIp.equals(LOCAL_IP) && source == 80 || dstIp.equals(LOCAL_IP) && destination == 80) {
                    log.debug("Ignore packet");
                    return;
                }

                log.debug("Receive packet: srcIp = {}, dstIp = {}, source = {}, destination = {}, seq = {}, ack = {}",
                        srcIp, dstIp, source, destination, seq, ack);

                // 判断是否携带http头信息
                if (pack.hasHeader(http)) {

                    // 解析HTTP请求头
                    log.debug("responseCode: {}", http.fieldValue(Http.Response.ResponseCode));
                    String responseCode = http.fieldValue(Http.Response.ResponseCode);
                    if (responseCode != null && !"200".equals(responseCode)) {
                        log.debug("response code not 200");
                        return;
                    }

                    String host = http.fieldValue(Http.Request.Host);
                    String requestUrl = http.fieldValue(Http.Request.RequestUrl);
                    String contentEncoding = http.fieldValue(Http.Response.Content_Encoding);
                    String contentLengthString = http.fieldValue(Http.Response.Content_Length);
                    if (contentLengthString == null) {
                        log.debug("Ignore packet without content length");
                        if (srcIp.equals(LOCAL_IP)) {
                            IGNORE_PORT.put(source, System.currentTimeMillis());
                        } else {
                            IGNORE_PORT.put(destination, System.currentTimeMillis());
                        }
                        return;
                    }
                    int contentLength = Integer.parseInt(contentLengthString);
                    String contentType = http.fieldValue(Http.Response.Content_Type);
                    log.debug("HTTP Content-Encoding: {}, Content-Length: {}, Content-Type: {}",
                            contentEncoding, contentLength, contentType);

                    // 解析用户请求头
                    String[] headerStrings = new String(http.getHeader()).split("\r\n");
                    Map<String, String> headers = new HashMap<>(headerStrings.length * 4);
                    for (String headerString : headerStrings) {
                        String[] kv = headerString.split(": ", 2);
                        if (kv.length == 2) {
                            headers.put(kv[0], kv[1]);
                        }
                    }

                    // 过滤非sdo服务器
                    if (host != null && !host.equals(SDO_HOST)) {
                        IGNORE_PORT.put(source, System.currentTimeMillis());
                        log.debug("Ignore not sdo host: {}", host);
                        return;
                    }

                    // 过滤黑名单url
                    if (BLACK_URL_SET.contains(requestUrl)) {
                        IGNORE_PORT.put(source, System.currentTimeMillis());
                        log.debug("Ignore black url: {}", requestUrl);
                        return;
                    }

                    // 获取payload，并转化为一个其他位置为null的list
                    byte[] payload = http.getPayload();
                    List<Byte> payloadList = new ArrayList<>(contentLength);
                    for (int i = 0; i < contentLength; i ++) {
                        if (i < payload.length) {
                            payloadList.add(payload[i]);
                        } else {
                            payloadList.add(null);
                        }
                    }

                    // 考虑到分块发送的情况，需要增加临时存储
                    HttpData httpData = new HttpData(
                            srcIp,
                            dstIp,
                            source,
                            destination,
                            seq + tcp.getPayload().length - payload.length,
                            ack,
                            payloadList,
                            contentEncoding,
                            contentLength,
                            contentType,
                            payload.length,
                            host,
                            requestUrl,
                            headers
                    );
                    if (srcIp.equals(LOCAL_IP)) {

                        // 将HTTP信息存入port
                        PAIR_MAP.put(source, new RequestResponsePair(httpData));

                    } else {

                        // port不存在
                        if (!PAIR_MAP.containsKey(destination)) {
                            log.error("Could not find destination: {}", destination);
                            return;
                        }

                        // ack与seq信息不符
                        long requestAck = PAIR_MAP.get(destination).getRequest().getAck();
                        if (requestAck != seq) {
                            log.error("requestAck: {} not equals to responseSeq: {}", requestAck, seq);
                            return;
                        }

                        // 更新请求-回应对
                        RequestResponsePair requestResponsePair = PAIR_MAP.get(destination);
                        requestResponsePair.setResponse(httpData);

                        // 判断是否已经完成
                        if (requestResponsePair.getResponse().getCurrentLength() >=
                                requestResponsePair.getResponse().getContentLength()) {
                            log.debug("HTTP request response pair resolve finish!");
                            Resolver.resolve(PAIR_MAP.remove(destination));
                        }

                    }

                } else {

                    // 获取payload，并转化为list
                    byte[] payload = tcp.getPayload();

                    // 加入新的payload信息
                    HttpData httpData;
                    if (srcIp.equals(LOCAL_IP)) {
                        if (!PAIR_MAP.containsKey(source)) {
                            log.error("Could not find source: {}", source);
                            return;
                        }
                        httpData = PAIR_MAP.get(source).getRequest();
                    } else {
                        if (!PAIR_MAP.containsKey(destination)) {
                            log.error("Could not find destination: {}", destination);
                            return;
                        }
                        httpData = PAIR_MAP.get(destination).getResponse();
                    }

                    // 更新payload
                    long seqDiff = seq - httpData.getSeq();
                    if (seqDiff < 0 || seqDiff + payload.length > httpData.getContentLength()) {
                        log.error("seq not match, start: {}, current: {}, length: {}",
                                httpData.getSeq(), seq, payload.length);
                        return;
                    }
                    int sizeCount = 0;
                    for (int i = 0; i < payload.length; i ++) {
                        int index = (int) (seqDiff + i);
                        if (httpData.getPayloadList().get(index) == null) {
                            sizeCount ++;
                        }
                        httpData.getPayloadList().set(index, payload[i]);
                    }

                    // 将数据增长量计入总数据量
                    httpData.setCurrentLength(httpData.getCurrentLength() + sizeCount);
                    log.debug("Total receive length: {}", httpData.getCurrentLength());

                    // 判断是否已经完成
                    if (dstIp.equals(LOCAL_IP) && httpData.getCurrentLength() >= httpData.getContentLength()) {
                        log.info("HTTP request response pair resolve finish!");
                        Resolver.resolve(PAIR_MAP.remove(destination));
                    }

                }

            }
        } catch (Exception e) {
            log.error("Resolve packet exception: {}", e.fillInStackTrace().toString());
        }

    }
}
