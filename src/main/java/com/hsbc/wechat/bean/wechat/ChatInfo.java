package com.hsbc.wechat.bean.wechat;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class ChatInfo implements Serializable {

    private static final long   serialVersionUID = -6365029519378512963L;

    //0表示成功，错误返回非0错误码
    private Long errcode;

    //返回信息，如非空为错误原因
    private String errmsg;

    //聊天记录数据内容
    private ArrayList<ChatData> chatData;

}
