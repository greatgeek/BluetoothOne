package com.panghui.bluetoothone;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceListDialogFrament extends DialogFragment {

    private static final String NO_DEVICE_DATA="没有搜索到蓝牙设备";

    private Button mBtnSearch;
    private ListView mListView;
    private ArrayAdapter<String> mListViewAdapter;
    private List<String> mDeviceList = new ArrayList<>();//对话框列数据内容

    private BluetoothAdapter mBtAdapter;//蓝牙适配器,这个类的重头戏

    private Callback mCallback;//回调接口，用于通信Mac 地址
    private Activity mActivity;//宿主Activity,在本应用中，即MainActivity

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("请选择连接设备");
        mActivity=getActivity();
        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        registerBluetoothReceier();
        openBluetooth();
        return inflater.inflate(R.layout.device_list,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView=(ListView)view.findViewById(R.id.lv);
        mListViewAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,mDeviceList);
        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mBtnSearch=(Button) view.findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBluetooth();
                if (mBtAdapter.isDiscovering()){
                    mBtAdapter.cancelDiscovery();
                    mBtnSearch.setText("重新搜索");
                }else{
                    doDiscovery();
                }
            }
        });

        doDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBtAdapter!=null){
            mBtAdapter.cancelDiscovery();
        }
        unregisterBluetoothReceiver();
    }



    /*********************function define********************************************************************/
    private void doDiscovery(){
        findPairedDevices();
        mBtAdapter.startDiscovery();
    }

    /**开启蓝牙*/
    public void openBluetooth(){
        if (!mBtAdapter.isEnabled()){
            Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }
    }

    /**获取手机上已匹配过的蓝牙设备*/
    private void findPairedDevices(){
        Set<BluetoothDevice> pairedDevices=mBtAdapter.getBondedDevices();

        if (mBtAdapter!=null&&mBtAdapter.isDiscovering()){
            mDeviceList.clear();
            mListViewAdapter.notifyDataSetChanged();
        }

        if (pairedDevices.size()>0){
            mDeviceList.clear();
            for (BluetoothDevice device : pairedDevices){
                String str=device.getName()+":"+device.getAddress();
                if (!mDeviceList.contains(str)){
                    mDeviceList.add(str);
                    mListViewAdapter.notifyDataSetChanged();
                }
            }
        }else{
            mDeviceList.clear();
            mListViewAdapter.notifyDataSetChanged();
        }
    }

    /**注册蓝牙广播接收器:为这个接收器注册了两个感兴趣的事件，一个是ACTION_FOUND,另一个是ACTION_DISCOVERY_FINISHED*/
    private void registerBluetoothReceier(){
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mActivity.registerReceiver(mReceiver,intentFilter);
    }

    /**注销蓝牙广播接收器*/
    public void unregisterBluetoothReceiver(){
        mActivity.unregisterReceiver(mReceiver);
    }

    public void setCallback(Callback callback){
        mCallback=callback;
    }

    /**************************anonymous class define*******************************************************/
    // Create a BroadcastReceiver for ACTION_FOUND
    private BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)){// ACTION_FOUND事件处理
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice btd=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (btd.getBondState()!=BluetoothDevice.BOND_BONDED){
                    mDeviceList.add(btd.getName()+":"+btd.getAddress());
                    mListViewAdapter.notifyDataSetChanged();
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){// ACTION——DIESCOVERY_FINISHED事件处理

                if (mListViewAdapter.getCount()==0){
                    mDeviceList.add(NO_DEVICE_DATA);
                    mListViewAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /*****************interface define****************************************************************/
    public interface Callback {
        void onSelectedItem(String address);
    }
}
