package com.hsbc.wechat.service.impl;

import com.hsbc.wechat.config.BussinessConfig;
import com.hsbc.wechat.service.WeChatContentService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

@Service
public class WeChatContentServiceImpl implements WeChatContentService {
    @Override
    public void doWeChatContent() {

    }



    private void writeSeqToLocal(int seq){
        String seqFilePath = BussinessConfig.getSeqFilepPth();
        FileOutputStream outputStream = null;
        try {
            File file = new File(seqFilePath);
            if(!file.exists()){file.mkdirs();}
            outputStream = new FileOutputStream(file);
            outputStream.write(seq);
        }catch ( Exception e){
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取本地seq
     * @return
     */
    private int getLocatSeq(){
        int seq = 0;
        String seqFilePath = BussinessConfig.getSeqFilepPth();
        BufferedReader bufferedReader = null;
        try {
            File file = new File(seqFilePath);
            if(file.exists()){
                bufferedReader = new BufferedReader(new FileReader(file));
                String seqStr = bufferedReader.readLine().trim();
                seq = Integer.parseInt(seqStr);
            }
        }catch ( Exception e){
            e.printStackTrace();
        }finally {
            if(bufferedReader!=null){
                try {
                    bufferedReader.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return seq;
        }

    }
}
