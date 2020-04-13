package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 同意协议消息bean
 * @author : like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class WeApp implements Serializable {

    private static final long serialVersionUID = 6797872948613174591L;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息描述
     */
    private String description;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 小程序名称
     */
    private String displayname;

}
