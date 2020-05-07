package com.hsbc.wechat.service;

public interface WechatApiService {
    void get(long seq,String strNow);

    boolean hasNext(long seq);
}
