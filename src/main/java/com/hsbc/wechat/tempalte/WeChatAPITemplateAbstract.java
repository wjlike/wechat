package com.hsbc.wechat.tempalte;


import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.util.ThreadLocalUtil;
import com.tencent.wework.Finance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author like
 * WeChat API Util
 */
@Slf4j
//@Component
public abstract class WeChatAPITemplateAbstract extends Finance {

    public long sdk = 0;

    //seq 默认为 0
    public long seq = 0;
    //每次获取的数量，最大值为1000
    public int limit = 1000;
    //初始化默认超长时间为60s
    public long timeout = 15;
    //初始化下载媒体超长时间
    public long mediaTimeOut = 30;

    public String corpid = BussinessConfig.getCorpid();

    public String secret = BussinessConfig.getSecret();

    public String priKey = BussinessConfig.getPrikey();

    public String proxy = "";
    public String paswd = "";

    public String mediaPath = BussinessConfig.getMediapath();

    public String downloadPath = BussinessConfig.getDownloadpath();


    /**
     * @param limit   每次获取的数量，最大值为1000
     * @param timeout 超时设置
     */
    public WeChatAPITemplateAbstract(int limit, long timeout) {
        this.timeout = timeout <= 0 ? this.timeout : timeout;
        this.limit = limit;
        init();
    }

    /**
     * 每次获取的数量，最大值为1000
     * 默认超时15s
     */
    public WeChatAPITemplateAbstract() {
        init();
    }

    /**
     * 初始化
     *
     * @return true:初始化成功
     * false：初始化失败
     */
    private boolean init() {
        sdk = NewSdk();
        int status = Init(sdk, corpid, secret);
        return status == 0 ? true : false;
    }

    private void DestroySdk(){
        if(ThreadLocalUtil.get().size()>0){return;}
        DestroySdk(sdk);
    }


}
