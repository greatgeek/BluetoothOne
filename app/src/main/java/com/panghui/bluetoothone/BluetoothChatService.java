package com.panghui.bluetoothone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.panghui.bluetoothone.base.AppConst;
import com.panghui.bluetoothone.utils.CloseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothChatService {

    private static final String NAME_SECURE="BluetoothChatSecure";
    private static final UUID MY_UUID_SECURE=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBtAdapter;
    private final Handler mHandler;// 由 UI Activity 通过构造器传过来
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE=0;// we're doing nothing
    public static final int STATE_CONNECTING=1;// now initiating an outgoing connection
    public static final int STATE_CONNECTED=2;// now connected to a remote device

    private String deviceAddress;

    /***********************function define********************************/
    public BluetoothChatService(Handler handler){
        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        mState=STATE_NONE;
        mHandler=handler;
    }

    private synchronized void setState(int state){
        mState=state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(AppConst.MESSAGE_STATE_CHANGE,state,-1).sendToTarget();
    }

    private synchronized void setState(int state,String deviceName){
        mState = state;
        // Given the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(AppConst.MESSAGE_STATE_CHANGE,state,-1,deviceName).sendToTarget();
    }

    public synchronized int getState(){ return mState; }

    /***Stop all threads**/
    public void stop(){
        setState(STATE_NONE);
        if (mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread=null;
        }
        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
    }

    public synchronized void connect(String address){
        // 说明连接的是同一个设备， 并且上次的连接请求还正在进行中或者正在通信中
        if (deviceAddress!=null && deviceAddress.equals(address) && mState!=STATE_NONE){
            return;
        }

        deviceAddress=address;// 存储最近一次进来的数据

        if (mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread=null;
        }
        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
        BluetoothDevice device=mBtAdapter.getRemoteDevice(address);
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);

    }

    public synchronized void connected(BluetoothSocket socket,BluetoothDevice device){
        if (mConnectThread!=null){
            mConnectThread=null;
        }

        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }

        setState(STATE_CONNECTED,device.getName());

        mConnectedThread=new ConnectedThread(socket,device);
        mConnectedThread.start();
    }

    private void sendConnectionFailed(){
        Message msg = mHandler.obtainMessage(AppConst.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.TOAST,"Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void sendConnectionLost(){
        Message msg = mHandler.obtainMessage(AppConst.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.TOAST,"Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void write(byte[] out){
        ConnectedThread r;
        synchronized (this) {
            if (mState!=STATE_CONNECTED){
                return;
            }
            r=mConnectedThread;
        }
        r.write(out);
    }

    /*********************class define************************************/
    // 连接为客户端
    private class ConnectThread extends Thread{
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        private ConnectThread(BluetoothDevice device){
            mmDevice = device;
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try{
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            }catch (IOException e){
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            mBtAdapter.cancelDiscovery();
            try{
                // Connect the device through the socket. This will block
                // until it success or throws an exception
                mmSocket.connect();
            }catch (IOException e){
                // Unable to connect; close the socket and get out
                CloseUtils.close(mmSocket);

                if (mmDevice.getAddress().equals(deviceAddress)){
                    sendConnectionFailed();
                    setState(STATE_NONE);
                }
                return;
            }

            synchronized (BluetoothChatService.this){
                mConnectThread=null;
            }
            connected(mmSocket,mmDevice);
        }

        // Will cancel an in-progress connection, and close the socket
        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread{// 管理连接
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;

        private ConnectedThread(BluetoothSocket socket, BluetoothDevice device){
            mmDevice = device;
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try{
                tmpIn=socket.getInputStream();
                tmpOut=socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            mmInputStream=tmpIn;
            mmOutputStream=tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[128]; // buffer store for the stream
            int len; // bytes returned from read()

            while(mState==STATE_CONNECTED){
                try{
                    // Read from the InputStream
                    len=mmInputStream.read(buffer);
                    byte[] buf_data = Arrays.copyOf(buffer,len);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(AppConst.MESSAGE_READ,-1,-1,buf_data).sendToTarget();
                }catch (IOException e){
                    CloseUtils.close(mmSocket);
                    synchronized (BluetoothChatService.this){
                        mConnectedThread=null;
                    }

                    if (mmDevice.getAddress().equals(deviceAddress)){
                        sendConnectionLost();
                        setState(STATE_NONE);
                    }
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] buffer){
            try{
                mmOutputStream.write(buffer);
                mHandler.obtainMessage(AppConst.MESSAGE_WRITE,-1,-1,new String(buffer)).sendToTarget();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
