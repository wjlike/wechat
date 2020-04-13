package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 同意协议消息bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Agree implements Serializable {

    private static final long serialVersionUID = 6797872948613174591L;

    /**
     * 同意协议者的userid
     */
    private String userid;

    /**
     * 外部企业默认用openid不用userid
     */
    private String openid;

    /**
     * 同意协议的时间
     */
    private Long agree_time;
}
