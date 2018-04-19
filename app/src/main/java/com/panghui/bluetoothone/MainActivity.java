package com.panghui.bluetoothone;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.panghui.bluetoothone.base.AppConst;
import com.panghui.bluetoothone.utils.HexUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private BluetoothChatService mChatService;

    private ImageView connect;
    private ImageView delete;
    private ImageView send;
    private TextView tv_label;
    private EditText et_send;
    private EditText et_receive;
    private CheckBox cb_hex;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        mChatService = new BluetoothChatService(mHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatService.stop();
    }

    /*********************function defien*********************************************/

    private void initView(){
        connect=(ImageView)findViewById(R.id.im_connect);
        delete=(ImageView)findViewById(R.id.im_delete);
        send=(ImageView)findViewById(R.id.im_send);
        tv_label=(TextView)findViewById(R.id.tv_label);
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
                //连接远程蓝牙
                mChatService.connect(address);
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
            case R.id.im_delete:
                et_receive.setText("");
                break;
            case R.id.im_send:
                mChatService.write(et_send.getText().toString().getBytes());
                break;
        }
    }

    private void scrollToBottom(final ScrollView scrollView, final View view){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (scrollView==null||view==null){
                    return;
                }

                int offset=view.getMeasuredHeight()-scrollView.getMeasuredHeight();
                if (offset<0){
                    offset=0;
                }

                scrollView.scrollTo(0,offset);
            }
        });
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            AppCompatActivity activity = MainActivity.this;
            switch (msg.what){
                case AppConst.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case BluetoothChatService.STATE_CONNECTED:
                            String deviceName = msg.obj.toString();
                            tv_label.setText("connected to "+ deviceName);
                            Toast.makeText(activity,"connected to "+deviceName,Toast.LENGTH_LONG).show();
                            //TODO
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tv_label.setText("connecting ...");
                            Toast.makeText(activity,"connecting ...",Toast.LENGTH_LONG).show();
                            //TODO
                            break;
                        case BluetoothChatService.STATE_NONE:
                            tv_label.setText("no connection");
                            //TODO
                            Toast.makeText(activity,"no connection", Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
                case AppConst.MESSAGE_WRITE:
                    String writeMessage = (String) msg.obj;
                    break;
                case AppConst.MESSAGE_READ:
                    byte[] buf = (byte[]) msg.obj;
                    String readTxt = new String(buf,0,buf.length);
                    if (cb_hex.isChecked()){
                        readTxt = HexUtils.bytesToHexStringWithSpace(buf);
                    }
                    et_receive.append(readTxt);
                    scrollToBottom(mScrollView,et_receive);
                    break;
                case AppConst.MESSAGE_TOAST:
                    Toast.makeText(activity,msg.getData().getString(AppConst.TOAST),Toast.LENGTH_LONG).show();
            }
        }
    };
}
