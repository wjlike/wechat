package com.hsbc.wechat.util;

import com.hsbc.wechat.config.BussinessConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HttpUtil {

    /**
     * 通过HTTP、HTTPS传送文件，
     * 使用jdk内置的net包完成，暂未调通
     * @param surl 服务器的文件上传url地址
     * @param file 待上传的文件
     * @return
     */
    /*
    public static boolean sendByHttp(String surl, File file) {
        OutputStream out = null;
        DataInputStream in = null;
        HttpURLConnection conn = null;
        try {
            // 创建Connection连接
            URL url = new URL(surl);
            conn = (HttpURLConnection)url.openConnection();
            // 创建POST请求
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Cache-Control","no-cache");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36");

            String bound = "----------" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + bound);

            conn.connect();
            conn.setConnectTimeout(100000);
            // 通过流的方式发送文件
            out = conn.getOutputStream();

            StringBuilder sb = new StringBuilder();
            sb.append("--");
            sb.append(bound);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n\r\n");
            out.write(sb.toString().getBytes());

            in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] buffer = new byte[1024];
            while ((bytes = in.read(buffer)) != -1) {
                out.write(buffer,0, bytes);
            }
            out.flush();
            conn.getInputStream();
            return true;
        } catch (Exception e) {
            System.out.println("发送文件出现异常！" + e);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e1) {}
        }
    }
     */

    /**
     * 通过 HTTP、HTTPS 进行文件上传，依赖 http-client 插件
     * @param url 上传地址
     * @param file 待上传的文件对象
     */
    public static void upload(String url, File file) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);
            FileBody bin = new FileBody(file);
            StringBody comment = new StringBody(file.getName(), ContentType.DEFAULT_BINARY);
            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", bin).addPart("filename", comment).build();
            httppost.setEntity(reqEntity);
            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String responseEntityStr = EntityUtils.toString(response.getEntity());
                    System.out.println(responseEntityStr);
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件上传windows服务器失败：" + e.toString());
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
        zipBaseDir = zipBaseDir.replace("\\", "/");
        if (!zipBaseDir.endsWith("/")) {
            zipBaseDir += "/";
        }
        boolean zipFlag = ZipUtil.zip(zipBaseDir + zipFileName, path);
        // 上传
        if (zipFlag) {
            String url = BussinessConfig.getHttpsUploadUrl() + "?total=" + fileNum[0];
            File zipFile = new File(zipBaseDir, zipFileName);
            HttpUtil.upload(url, zipFile);

            // 将文件夹剪切到备份目录
            movePath(file);
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
            for (File f : children) {
                getChildrenNum(f, num);
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
        /*
        //剩下一堆空文件夹怎么处理？删除？删除会误删其他正在从微信下载的文件。
        try {
            srcFile.delete();
            System.out.println("delete file=" + srcFile.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }
        */
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
            System.out.println("move srcFile=" + srcFile.getAbsolutePath());
            System.out.println("move destFile=" + destFile.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
//        File file = new File("C:\\Users\\lanruijin\\Desktop\\tmp\\31414.rar");
//        HttpUtil.upload("http://localhost:8080/test/upload?total=1", file);

        int[] fileNum = new int[] { 0 };
        getChildrenNum(new File("C:\\Users\\lanruijin\\Desktop\\wechat-like"), fileNum);
        System.out.println("fileNum=" + fileNum[0]);
    }
}
