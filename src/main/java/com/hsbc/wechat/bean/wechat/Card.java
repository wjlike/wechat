package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 名片bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Card implements Serializable {

    private static final long serialVersionUID = 7324643844902425243L;

    /**
     * 名片所有者所在的公司名称
     */
    private String corpname;

    /**
     * 名片所有者的id，同一公司是userid
     */
    private String userid;

    /**
     * 不同公司是openid
     */
    private String openid;

}
