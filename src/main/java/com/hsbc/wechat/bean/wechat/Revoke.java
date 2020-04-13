package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 撤回消息Bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Revoke implements Serializable {

    private static final long serialVersionUID = 8334345320444946475L;

    /**
     * 标识撤回的原消息的msgid
     */
    private String pre_msgid;

}
