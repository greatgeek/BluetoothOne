package com.panghui.bluetoothone.base;

public class AppConst {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE=1;
    public static final int MESSAGE_READ=2;
    public static final int MESSAGE_WRITE=3;
    public static final int MESSAGE_DEVICE_NAME=4;
    public static final int MESSAGE_TOAST=5;

    public static final int BIKE_STATUS_LOCKED=6;
    public static final int BIKE_STATUS_UNLOCKed=7;

    public static final String TOAST="toast";

    public static final int SEND_UNLOCK_SIGNAL=8;
    public static final int SEND_LOCK_SIGNAL=9;
}
