package com.example.zz.downloaddemo.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zz.downloaddemo.R;
import com.example.zz.downloaddemo.bean.FileInfo;
import com.example.zz.downloaddemo.services.DownLoadService;

/**
 * 类描述：断点续传工具类
 * 创建人：zz
 * 创建时间：2017/7/18 16:24
 */
public class MainActivity extends Activity implements View.OnClickListener{

    private TextView tvFileName;
    private ProgressBar pbProgress;
    private Button buttonStart;
    private Button buttonStop;
    private String FileUrl = "http://sw.bos.baidu.com/sw-search-sp/software/f67ac4724239d/duba170713_2017.11.5.5_setup.exe";
    private FileInfo fileInfo;
    private MyReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //   initWindow();
        initView();
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter(DownLoadService.UPDATE_ACTION);
       registerReceiver(myReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);

    }

    /**
     * 初始化Window上层窗体
     */
    private void initWindow() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.RGBA_8888;

        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        layoutParams.gravity = Gravity.CENTER;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        layoutParams.x = width / 3;
        layoutParams.y = height / 3;

        View mFloatLayout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.window_item,null);

        windowManager.addView(mFloatLayout, layoutParams);
    }

    /**
     * 初始化UI
     */
    private void initView() {
        tvFileName = ((TextView) findViewById(R.id.tv_filename));
        pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
        pbProgress.setMax(100);
        buttonStart = ((Button) findViewById(R.id.button_start));
        buttonStop = ((Button) findViewById(R.id.button_stop));
        fileInfo = new FileInfo(0,FileUrl,"金山下载",0,0);
        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), DownLoadService.class);
        intent.putExtra("fileInfo",fileInfo);
        switch (view.getId()){
            case R.id.button_start:
                intent.setAction(DownLoadService.START_ACTION);
                startService(intent);
                break;
            case R.id.button_stop:
                intent.setAction(DownLoadService.STOP_ACTION);
                startService(intent);
                break;
        }
    }


    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(DownLoadService.UPDATE_ACTION.equals(intent.getAction())){
                int finished = intent.getIntExtra("finished", 0);
                pbProgress.setProgress(finished);
            }
        }
    }
}
