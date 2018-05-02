package com.panghui.bluetoothone.http;

import android.os.Handler;
import android.util.Log;

import com.panghui.bluetoothone.base.AppConst;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {

    static public void UpdateLocation(final String url,final String user,final String latitude,final String longtitude){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("user", user)
                            .add("latitude", latitude)
                            .add("longtitude", longtitude)
                            .build();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static public void GetBikeUnlockStatus(final Handler mHandler,final String url,final String bikeID){
        new Thread(new Runnable() {
            @Override
            public void run() {

                    try {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("bikeID", bikeID)
                                .build();
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        Response response;
                        String status;

                        while (true) {
                            response = client.newCall(request).execute();
                            if (response.code() == 200) {
                                status = response.body().string();
                                Log.d("BikeStatus:", status);
                                if (status.equals("unlock")) {
                                    mHandler.obtainMessage(AppConst.BIKE_STATUS_UNLOCKed).sendToTarget();
                                    mHandler.obtainMessage(AppConst.SEND_UNLOCK_SIGNAL).sendToTarget();//发送解锁动作信号
                                    break;
                                } else {
                                    //mHandler.obtainMessage(AppConst.BIKE_STATUS_LOCKED).sendToTarget();
                                }
                            }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                }
        }).start();
    }

    static public void GetBikeLockStatus(final Handler mHandler,final String url,final String bikeID){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("bikeID",bikeID)
                            .build();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Response response;
                    String status;
                    while(true){
                        response = client.newCall(request).execute();
                        if (response.code()==200){
                            status = response.body().string();
                            if (status.equals("lock")){
                                mHandler.obtainMessage(AppConst.BIKE_STATUS_LOCKED).sendToTarget();
                                mHandler.obtainMessage(AppConst.SEND_LOCK_SIGNAL).sendToTarget();//发送上锁动作信号
                                break;
                            }else{
                                //mHandler.obtainMessage(AppConst.BIKE_STATUS_UNLOCKed).sendToTarget();
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
