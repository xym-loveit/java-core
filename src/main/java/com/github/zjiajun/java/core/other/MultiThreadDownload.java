package com.github.zjiajun.java.core.other;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhujiajun
 * 15/7/19 19:35
 */
public class MultiThreadDownload {


    private static final String DOWNLOAD_PATH =
        /*
        md5
        98e821d2755ae3f8e6d66536fcdaa1b0 *apache-tomcat-8.0.32.tar.gz
        sha1
        d063269b5fe67c312f8b50106d94aa8d80505582 *apache-tomcat-8.0.32.tar.gz
         */
        "http://mirrors.cnnic.cn/apache/tomcat/tomcat-8/v8.0.32/bin/apache-tomcat-8.0.32.tar.gz"; //8594 kb

    //线程数量
    private static final int THREAD_NUM = 4;

    public static void main(String[] args) throws IOException {

        URL url = new URL(DOWNLOAD_PATH);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        int responseCode = httpURLConnection.getResponseCode();
        if (200 == responseCode) {
            int fileSize = httpURLConnection.getContentLength();
            System.out.println("文件大小 :" + fileSize/1024 + "KB");
            //生产临时文件
            File file = new File(System.getProperty("user.home") + "/Work/temp.zip");
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
            randomAccessFile.setLength(fileSize);
            randomAccessFile.close();

            int block = fileSize / THREAD_NUM; //文件大小除以线程数量,相当于每个线程下载多少块内容
            System.out.println(THREAD_NUM + "个线程下载,每个线程下载: " + block / 1024 + "KB");
            ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_NUM);
            for (int threadId = 1; threadId <= THREAD_NUM; threadId++) {
                threadPool.execute(new DownloadThread(url,fileSize,block,threadId,file));
            }
            threadPool.shutdown();
        }
    }


    /**
     * (1-1)*2420609= 0;        (1*2420609)-1 = 2420608
     * (2-1)*2420609= 2420609;  (2*2420609)-1 = 4841217
     * (3-1)*2420609= 4841218;  (3*2420609)-1 = 7261826
     * (4-1)*2420609= 7261827;  9682439(contentLength)
     */
    private static class DownloadThread implements Runnable {

        private URL url;
        private int threadId;
        private File file;
        private int startIndex,endIndex;

        public DownloadThread(URL url, int fileSize,int block, int threadId, File file) {
            this.url = url;
            this.threadId = threadId;
            this.file = file;
            startIndex = (threadId - 1) * block; //起始位置 ＝ (线程Id - 1) * block
            if (threadId == THREAD_NUM) {
                endIndex = fileSize; //如果是最后一个线程,结束位置 = 文件大小
            } else {
                endIndex = (threadId * block) - 1; //不是最后的一个的结束位置 = (线程Id * block) -1
            }


        }

        @Override
        public void run() {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Range","bytes=" + startIndex + "-" + endIndex);
                if (206 == httpURLConnection.getResponseCode()) {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
                    //移动指针至该线程负责写入数据的位置。
                    randomAccessFile.seek(startIndex);
                    InputStream inputStream = httpURLConnection.getInputStream();
                    byte [] b = new byte[1024];
                    int len;
                    while ((len = inputStream.read(b)) != -1) {
                        randomAccessFile.write(b,0,len);
                    }
                    System.out.println("线程" + threadId + "下载完毕");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
