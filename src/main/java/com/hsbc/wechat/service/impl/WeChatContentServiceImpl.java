package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.service.WeChatContentService;
import com.hsbc.wechat.service.WechatApiService;
import com.hsbc.wechat.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

@Service
public class WeChatContentServiceImpl implements WeChatContentService {
    @Autowired
    WechatApiService wechatApiService;
    @Override
    public void doWeChatContent() {

        long seq = getLocatSeq();
        wechatApiService.get(seq);

    }

    /**
     * 读取本地seq
     * @return
     */
    private long getLocatSeq(){
       String str =  FileUtil.readStringFromFile(BussinessConfig.getSeqFilepPth());
       if("".equals(str)){
           str = "0";
       }
       return Long.getLong(str);
    }
}
