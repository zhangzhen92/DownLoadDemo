package com.example.zz.downloaddemo.services;

import android.content.Context;
import android.content.Intent;

import com.example.zz.downloaddemo.bean.FileInfo;
import com.example.zz.downloaddemo.bean.ThreadInfo;
import com.example.zz.downloaddemo.db.ThreadDao;
import com.example.zz.downloaddemo.db.ThreadDaoImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 类描述：文件下载类
 * 创建人：zz
 * 创建时间： 2017/7/19 17:00
 */


public class DownLoadTask {

    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDao threadDao;
    private int mFinished = 0;     //已下载的大小
    public boolean isPause = false;   //是否暂停

    public DownLoadTask(Context context, FileInfo fileInfo) {
        this.mContext = context;
        this.mFileInfo = fileInfo;
        threadDao = new ThreadDaoImpl(context);
    }

    public void downLoad(){
        List<ThreadInfo> threads = threadDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if(threads.size() == 0){
             threadInfo = new ThreadInfo(0,mFileInfo.getUrl(),0,mFileInfo.getLength(),0);
        }else {
            threadInfo = threads.get(0);
        }
        DownLoadThread thread = new DownLoadThread(threadInfo);
        thread.start();
    }

    class DownLoadThread extends Thread {
        private ThreadInfo mThreadInfo;

        public DownLoadThread(ThreadInfo threadInfo) {
            this.mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                //向数据库插入线程信息
                //设置文件写入位置
                //开始下载
                if (!threadDao.threadIsExists(mFileInfo.getUrl(), mFileInfo.getId())) {
                    threadDao.insertThread(mThreadInfo);
                }
                //设置下载位置
                URL url = new URL(mFileInfo.getUrl());
                 conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd()); //可以设置下载开始的位置结束的位置

                File file = new File(DownLoadService.DOWN_LOAD,mFileInfo.getFileName());
                 raf = new RandomAccessFile(file,"rwd");        //随机访问文件流
                raf.seek(start);             //在读写文件的时候，跳过指定字节数，从所在位置的下一位进行读写

              Intent intent = new Intent(DownLoadService.UPDATE_ACTION);
                //开始下载
                mFinished += mThreadInfo.getFinished();
                if(conn.getResponseCode() == 206){
                    //读取数据
                     inputStream = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len  = -1;
                    long time = System.currentTimeMillis();
                    while((len = inputStream.read(buffer)) != -1){
                      raf.write(buffer,0,len);
                        mFinished += len;
                        long currentTime = System.currentTimeMillis();
                        if(System.currentTimeMillis() - time > 2000){
                            int progress = mFinished *100 / mFileInfo.getLength();
                            intent.putExtra("finished",progress);
                            mContext.sendBroadcast(intent);
                            time = System.currentTimeMillis();
                        }
                        if(isPause){
                            threadDao.updateThread(mFileInfo.getUrl(),mThreadInfo.getId(),mFinished);
                            return;

                        }
                    }

                    threadDao.deleteThread(mFileInfo.getUrl(),mThreadInfo.getId());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    conn.disconnect();
                    raf.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
