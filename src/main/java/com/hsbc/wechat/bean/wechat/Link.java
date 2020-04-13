package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 链接消息bean
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class Link implements Serializable {

    private static final long serialVersionUID = -2135900779871468796L;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息描述
     */
    private String description;

    /**
     * 链接url地址
     */
    private String link_url;

    /**
     * 链接图片url
     */
    private String image_url;

}
