package com.hsbc.wechat.service;

public interface WechatApiService {
    void get(long seq);

    boolean hasNext(long seq);
}
