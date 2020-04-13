package com.hsbc.wechat.bean.wechat;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author like
 * @date : 2020年4月13日10:19:42
 */
@Data
public class ChatData implements Serializable {

    private static final long serialVersionUID = -5422670726212800062L;

    //message seq
    private Long seq;

    //messge id
    private String msgid;

    //message version
    private int               publickey_ver;

    //message key be encrypted
    private String encrypt_random_key;

    //message  be encrypted
    private String encrypt_chat_msg;

}
