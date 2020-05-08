package com.hsbc.wechat.util;

import com.alibaba.fastjson.JSONObject;
import com.hsbc.wechat.config.BussinessConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class HttpUtil {

    /**
     * 通过 HTTP、HTTPS 进行文件上传，依赖 http-client 插件
     * @param url 上传地址
     * @param file 待上传的文件对象
     */
    public static void upload(String url, File file) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
            httppost.setConfig(requestConfig);
            FileBody bin = new FileBody(file);
            StringBody comment = new StringBody(file.getName(), ContentType.TEXT_PLAIN);
            HttpEntity reqEntity = MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE).addPart("file", bin).addPart("filename", comment).build();
            httppost.setEntity(reqEntity);
            //log.info("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                //log.info(response.getStatusLine().toString());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String responseEntityStr = EntityUtils.toString(response.getEntity());
                    log.info(responseEntityStr);
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.info("文件上传windows服务器失败：" + e.toString());
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件压缩并上传到 windows 服务器
     * @param path 待压缩和上传的路径
     */
    public static void zipAndUpload(String path) {
        File file = new File(path);
        // 获取文件个数
        int[] fileNum = new int[] { 0 };
        getChildrenNum(file, fileNum);
        // 压缩，文件名格式（含日期时间）：wechat_20200427_182841.zip
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String zipFileName = "wechat_" + sdf.format(new Date()) + ".zip";
        String zipBaseDir = BussinessConfig.getZipPath();
        //String zipBaseDir = "d:/tmp/test/wechat/zip";
        zipBaseDir = zipBaseDir.replace("\\", "/");
        if (!zipBaseDir.endsWith("/")) {
            zipBaseDir += "/";
        }
        String zipFileNameReal = zipBaseDir + zipFileName;
        long startTime = System.currentTimeMillis();
        boolean zipFlag = ZipUtil.zip(zipFileNameReal, path);

        //记录日志
        long fileSize = new File(zipFileNameReal).length();
        String logContent = "文件压缩\t status:" + zipFlag + "\t filesNum:" + fileNum[0]
                + "\t fileSize(byte):" + fileSize
                + "\t costTime(ms):" + (System.currentTimeMillis() - startTime)
                + "\t zipFileName:" + zipFileNameReal;
        log(logContent);

        // 上传
        if (zipFlag) {
            String url = BussinessConfig.getHttpsUploadUrl() + "?total=" + fileNum[0];
            //String url = "http://localhost:12345/filereceive/upload" + "?total=" + fileNum[0];
            log.info("文件上传：" + url);
            File zipFile = new File(zipBaseDir, zipFileName);
            startTime = System.currentTimeMillis();
            HttpUtil.upload(url, zipFile);

            //记录日志
            logContent = "文件上传\t zipFile:" + zipBaseDir + zipFileName
                    + "\t fileSize(byte):" + fileSize
                    + "\t costTime(ms):" + (System.currentTimeMillis() - startTime)
                    + "\t url:" + url;
            log(logContent);

            // 将文件夹剪切到备份目录
            movePath(file);
        }

    }
    /**
     * 文件压缩并上传到 windows 服务器
     * @param path 待压缩和上传的路径
     */
    public static void folderUpload(String path) {
        File file = new File(path);
        // 获取文件个数
        int[] fileNum = new int[] { 0 };
        getChildrenNum(file, fileNum);
        sendFile(file,fileNum);

        // 将文件夹剪切到备份目录
        movePath(file);

    }

    /**
     * 递归发送文件
     * @param file
     * @param fileNum
     */
    private static void sendFile(File file, int[] fileNum) {
        int limit = 0;
        String url = BussinessConfig.getHttpsUploadUrl() + "?total=" + fileNum[0]+"&present=";
        if (file.isFile()) {
            limit++;
            HttpUtil.upload(url, file);
        } else {
            File[] children = file.listFiles();
            for (File f : children) {
                sendFile(f, fileNum);
            }
        }
    }

    /**
     * 获取某文件夹下的文件个数
     * @param file 文件夹对应的 File 对象
     * @param num 存储文件个数结果的 1*1 数组
     */
    private static void getChildrenNum(File file, int[] num) {
        if (file.isFile()) {
            num[0] += 1;
        } else {
            File[] children = file.listFiles();
            if (children != null) {
                for (File f : children) {
                    getChildrenNum(f, num);
                }
            }
        }
    }

    /**
     * 文件夹剪切，已上传成功的文件夹，挪到备份文件夹。
     * 全部上传完成后，统一迁移。（已经在SftpFileUtil中实现，该方法实际只删除空文件夹）
     * 如果需要单个文件上传完成马上迁移，需修改文件上传公用类
     * @param srcFile
     */
    private static void movePath(File srcFile) {
        //文件则移动，文件夹则递归
        if (srcFile.isFile()) {
            moveFile(srcFile);
        } else {
            File[] childrenFile = srcFile.listFiles();
            if (childrenFile != null) {
                for (File f : childrenFile) {
                    movePath(f);
                }
            }
        }
    }

    /**
     * 文件剪切，已上传成功的文件，挪到备份文件夹
     * @param srcFile
     */
    private static void moveFile(File srcFile) {
        //转换根目录为标准目录，消除路径中正反斜杠的混乱问题
        String localRoot = BussinessConfig.getDownloadpath().replace("\\", "/");
        String localBakRoot = BussinessConfig.getBakFilePath().replace("\\", "/");
        //获取原文件的绝对路径和相对路径
        String srcPath = srcFile.getAbsolutePath();
        String subPath = srcPath.substring(localRoot.length());
        //目标文件
        File destFile = new File((localBakRoot + "/" + subPath).replace("//", "/"));
        File parent = destFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        //文件剪切（重命名）
        try {
            srcFile.renameTo(destFile);
            log.info("Move File, srcFile=" + srcFile.getAbsolutePath() + " destFile=" + destFile.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\lanruijin\\Desktop\\tmp\\程序打包工具-精简.zip");
        HttpUtil.upload("http://localhost:12345/filereceive/upload?total=1", file);

//        HttpUtil.zipAndUpload("D:\\tmp\\test\\wechat\\2020");

//        int[] fileNum = new int[] { 0 };
//        getChildrenNum(new File("C:\\Users\\lanruijin\\Desktop\\tmp\\test1"), fileNum);
//        log.info("fileNum=" + fileNum[0]);
    }

    /**
     *  getRequest
     * @param url
     * @return
     * @throws IOException
     */
    public static JSONObject doGetJsonByUrl(String url) throws IOException {
        log.info("request url : {}",url);
        JSONObject jsonObject = null;
        HttpClient client = null;
        HttpGet httpGet = null;
        try {
            client = HttpClientBuilder.create().build();
            httpGet = new HttpGet(url);
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (IOException e){
            e.printStackTrace();
        }finally {
            httpGet.releaseConnection();
            return jsonObject;
        }
    }

    private static void log(String content) {
        String logPath = BussinessConfig.getLogPath();
        String fileName = "http_log_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".log";
        logPath = (logPath + "/" + fileName).replace("\\", "/").replace("//", "/");
        BufferedWriter bw = null;
        File file = new File(logPath);
        File parent = file.getParentFile();
        if(!parent.exists()){parent.mkdirs();}
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(content);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            log.error("写http日志失败:" + e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (Exception e) {}
            }
        }
    }

}
