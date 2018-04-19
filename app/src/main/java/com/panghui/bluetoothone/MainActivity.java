package com.panghui.bluetoothone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView connect;
    private ImageView delete;
    private ImageView send;
    private EditText et_send;
    private EditText et_receive;
    private CheckBox cb_hex;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    /*********************function defien*********************************************/

    private void initView(){
        connect=(ImageView)findViewById(R.id.im_connect);
        delete=(ImageView)findViewById(R.id.im_delete);
        send=(ImageView)findViewById(R.id.im_send);
        et_send=(EditText)findViewById(R.id.et_send);
        et_receive=(EditText)findViewById(R.id.et_receive);
        cb_hex=(CheckBox)findViewById(R.id.cb_hex);
        mScrollView=(ScrollView)findViewById(R.id.scrol_view);

        connect.setOnClickListener(this);
        delete.setOnClickListener(this);
        send.setOnClickListener(this);
    }

    private void showDialog(){
        DeviceListDialogFrament dialog=new DeviceListDialogFrament();
        dialog.setCallback(new DeviceListDialogFrament.Callback() {
            @Override
            public void onSelectedItem(String address) {

            }
        });
        dialog.show(MainActivity.this.getSupportFragmentManager(),"DeviceList");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.im_connect:
                showDialog();
                break;
        }
    }
}
