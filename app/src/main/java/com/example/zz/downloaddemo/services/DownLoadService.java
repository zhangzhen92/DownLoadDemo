package com.example.zz.downloaddemo.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.zz.downloaddemo.bean.FileInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 类描述：下载service
 * 创建人：zz
 * 创建时间： 2017/7/19 10:22
 */


public class DownLoadService extends Service{
     public static final String START_ACTION = "START_ACTION";
    public static final String STOP_ACTION = "START_STOP";
    public static final String UPDATE_ACTION = "UPDATE_ACTION";
    private static final String TAG = "DownLoadService";
    private static  final  int MSG_INIT = 1;
    public static final String DOWN_LOAD = Environment.getExternalStorageDirectory().getAbsolutePath()+
            "/down_loads/";
    private DownLoadTask task = null;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;       //用于检验文件是否创建成功
                    task = new DownLoadTask(DownLoadService.this,fileInfo);
                    task.downLoad();
                    break;
            }
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(START_ACTION.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
          new InitThread(fileInfo).start();
        }else if(STOP_ACTION.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            if(task != null){
                task.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    class InitThread extends Thread{
        private FileInfo fileInfo;
        public InitThread(FileInfo fileInfo) {
         this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
           try {
               //网络连接      获取文件长度     创建本地文件
               URL url = new URL(fileInfo.getUrl());
               conn = (HttpURLConnection) url.openConnection();
               conn.setRequestMethod("GET");
               conn.setConnectTimeout(5000);
               int length = -1;
               if(conn.getResponseCode() == 200){
                 length = conn.getContentLength();
               }
               if(length <= 0){
                   return;
               }
               File dir = new File(DOWN_LOAD);
               if(!dir.exists()){
                   dir.mkdirs();
               }
               File file = new File(dir,fileInfo.getFileName());     //创建文件
               raf = new RandomAccessFile(file,"rwd");           //rwd       read  write delete  操作模式
               raf.setLength(length);
               fileInfo.setLength(length);
               mHandler.obtainMessage(MSG_INIT,fileInfo).sendToTarget();
           }catch (Exception e){
               e.printStackTrace();
           }finally {
               try {
                   conn.disconnect();
                   raf.close();
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
        }
    }
}
