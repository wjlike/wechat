package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 消息信息Bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Text implements Serializable {

    private static final long serialVersionUID = -4701067699228819936L;

    /**
     * 消息内容
     */
    private String content;

}
