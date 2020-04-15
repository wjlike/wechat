package com.hsbc.wechat.tempalte;

import com.alibaba.fastjson.JSONObject;
import com.hsbc.wechat.bean.wechat.ChatInfo;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class WeChatInfoAPI extends  WeChatAPITemplateAbstract implements WechatApiService {
    /**
     * 注：获取会话记录内容不能超过3天
     * @param seq 本次请求获取消息记录开始的seq值。默认从0开始，非首次使用上次企业微信返回的最大seq。允许从任意seq重入拉取。Uint64类型，范围0-pow(2,64)-1
     */
    ExecutorService service = Executors.newFixedThreadPool(10);
    private void getChatData(long seq){
        seq = Math.max(seq, 0);
        ChatInfo chatInfo = null;
        //返回本次拉取消息的数据.密文消息
        long slice = NewSlice();
        int ret = GetChatData(sdk, seq, limit, proxy, paswd, timeout, slice);
        if (ret != 0) {
            // log.error("getchatdata ret :()",ret);
            throw new RuntimeException("get WeChat Content Error: "+ret);
        }
        String data = GetContentFromSlice(slice);

        chatInfo = JSONObject.parseObject(data,ChatInfo.class);

        getMaxSeq(chatInfo);
        //异步处理
        WeChatMediaAPI weChatMediaAPI = new WeChatMediaAPI();
        weChatMediaAPI.setChatInfo(chatInfo);
        service.submit(weChatMediaAPI);
        service.shutdown();

    }
    private void getMaxSeq(ChatInfo chatInfo) {
        chatInfo.getChatData().forEach(chatData->{
            this.seq = Math.max(chatData.getSeq(),this.seq);
        });
    }


    @Override
    public long get(long seq) {
        getChatData(seq);
        return seq;
    }
}
