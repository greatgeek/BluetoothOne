package com.panghui.bluetoothone.http;

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
}
