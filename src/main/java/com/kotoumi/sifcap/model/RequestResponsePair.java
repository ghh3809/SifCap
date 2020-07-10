package com.kotoumi.sifcap.model;

import lombok.Data;

@Data
public class RequestResponsePair {

    private long timestamp;
    private HttpData request;
    private HttpData response;

    public RequestResponsePair(HttpData request) {
        this.timestamp = System.currentTimeMillis();
        this.request = request;
    }

}
