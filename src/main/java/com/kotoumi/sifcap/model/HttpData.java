package com.kotoumi.sifcap.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HttpData {

    private String srcIp;
    private String dstIp;
    private int source;
    private int destination;
    private long seq;
    private long ack;
    private List<Byte> payloadList;
    private String contentEncoding;
    private int contentLength;
    private String contentType;
    private int currentLength;
    private String host;
    private String requestUrl;
    private Map<String, String> headers;
    private String originRequestData;

    public HttpData(String srcIp, String dstIp, int source, int destination, long seq, long ack, List<Byte> payloadList,
                    String contentEncoding, int contentLength, String contentType, int currentLength, String host,
                    String requestUrl, Map<String, String> headers) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.source = source;
        this.destination = destination;
        this.seq = seq;
        this.ack = ack;
        this.payloadList = payloadList;
        this.contentEncoding = contentEncoding;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.currentLength = currentLength;
        this.host = host;
        this.requestUrl = requestUrl;
        this.headers = headers;
    }

}
