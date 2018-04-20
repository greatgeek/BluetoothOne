package com.panghui.bluetoothone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.panghui.bluetoothone.base.AppConst;
import com.panghui.bluetoothone.http.HttpUtils;
import com.panghui.bluetoothone.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,AMapLocationListener{
    /**蓝牙部分*/
    private BluetoothChatService mChatService;

    private ImageView connect;
    private ImageView delete;
    private ImageView send;
    private TextView tv_label;
    private EditText et_send;
    private EditText et_receive;
    private CheckBox cb_hex;
    private ScrollView mScrollView;

    /**定位部分*/
    //声明 AMapLocationClient 类对象
    public AMapLocationClient mLocationClient=null;
    //已实现回调监听器的接口

    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption=null;
    private Context mContext;
    double mylatitude;
    double mylongtitude;

    /**网络部分*/
    private String updateLocationUrl="http://120.79.91.50/DreamBike/DreamBike_updateLocation.php";
    private String GetBikeStatusUrl="http://120.79.91.50/DreamBike/DreamBike_bluetoothlockSlave.php";
    private boolean isFristGetBikeStatus=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=getApplicationContext();
        initView();
        mChatService = new BluetoothChatService(mHandler);
        /**注册权限*/
        registerPermissions();
        /**获取定位服务*/
        location();

    }

    /**定位*/
    public void location(){
        mLocationClient = new AMapLocationClient(mContext);
        mLocationClient.setLocationListener(this);
        mLocationOption=new AMapLocationClientOption();
        //高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //自定义连续定位
        mLocationOption.setInterval(5000);
        //不需要返回地址信息
        mLocationOption.setNeedAddress(false);

        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatService.stop();
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //TODO

                }else{
                    Toast.makeText(this, "发生未知错误！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /*********************function defien*********************************************/
    private void registerPermissions(){
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            //TODO

        }
    }

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
                            Toast.makeText(activity,"connected to "+deviceName,Toast.LENGTH_SHORT).show();
                            //TODO
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tv_label.setText("connecting ...");
                            Toast.makeText(activity,"connecting ...",Toast.LENGTH_SHORT).show();
                            //TODO
                            break;
                        case BluetoothChatService.STATE_NONE:
                            tv_label.setText("no connection");
                            //TODO
                            Toast.makeText(activity,"no connection", Toast.LENGTH_SHORT).show();
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
                    break;
                case AppConst.BIKE_STATUS_UNLOCKed:
                    Toast.makeText(activity,"It's status is Unlocked!",Toast.LENGTH_LONG).show();
                    et_receive.setText("unlocked!");
                    break;
                case AppConst.BIKE_STATUS_LOCKED:
                    Toast.makeText(activity,"It's still locked!",Toast.LENGTH_SHORT).show();
                    et_receive.setText("locked!");
                    break;
                default:
            }
        }
    };

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode()==0){
            mylatitude=aMapLocation.getLatitude();
            mylongtitude=aMapLocation.getLongitude();
            String lat=Double.toString(mylatitude);
            String lon=Double.toString(mylongtitude);
            Toast.makeText(MainActivity.this,lat+"|"+lon,Toast.LENGTH_SHORT).show();
            HttpUtils.UpdateLocation(updateLocationUrl,"hope1",lat,lon);
            if (isFristGetBikeStatus){
                HttpUtils.GetBikeStatus(mHandler,GetBikeStatusUrl,"hope1");
                isFristGetBikeStatus=false;
            }
        }else{
            Log.e("AmapError","location Error, ErrorCode:"
            +aMapLocation.getErrorCode()+",errorInfo:"+aMapLocation.getErrorInfo());
        }
    }


}
