package com.hsbc.wechat.service;

import org.springframework.scheduling.annotation.AsyncResult;

public interface AsycWechatApiService {
    AsyncResult<Boolean> get(long seq);
}
