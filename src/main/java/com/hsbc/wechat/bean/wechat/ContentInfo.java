package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class ContentInfo implements Serializable {

    private static final long serialVersionUID = 6052530845022305477L;
    /**
     * seq
     */
    private Long seq;
    /**
     * 消息id，消息的唯一标识
     */
    private String msgid;

    /**
     * 消息动作，撤回消息是为recall，其他为send
     */
    private String action;

    /**
     * 消息发送方id,userid
     */
    private String from;

    /**
     * 消息接收方列表，可能是多个，同一个企业内容未userid，非相同企业为openid
     */
    private ArrayList<String> tolist;

    /**
     * 群聊消息的群id
     */
    private String roomid;

    /**
     * 消息发送时间戳
     */
    private Long msgtime;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 文本类型
     */
    private Text              text;

    /**
     * 图片类型
     */
    private Image             image;

    /**
     * 撤回消息类型
     */
    private Revoke            revoke;

    /**
     * 同意协议消息类型
     */
    private Agree             agree;

    /**
     * 语音消息
     */
    private Voice             voice;

    /**
     * 视频消息
     */
    private Video             video;

    /**
     * 名片类型
     */
    private Card              card;

    /**
     * 地址位置信息
     */
    private Location          location;

    /**
     * 地址位置信息
     */
    private Emotion           emotion;

    /**
     * 地址位置信息
     */
    private File              file;

    /**
     * 地址位置信息
     */
    private Link              link;

    /**
     * 地址位置信息
     */
    private WeApp weapp;


}
