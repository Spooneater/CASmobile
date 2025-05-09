package com.example.attendancechecker;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class BluetoothReader {
    HashSet<String> addresses;
    BluetoothAdapter bluetoothAdapter;
    Context context;
    @RequiresApi(api = Build.VERSION_CODES.S)
    public BluetoothReader(Context context){
        this.context = context;
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        addresses = new HashSet<String>();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},2);
        }
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.BLUETOOTH_SCAN},2);
        }
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        context.registerReceiver(receiver,intentFilter);

        bluetoothAdapter.startDiscovery();

    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) {

                    return;
                }
                String deviceName;
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    deviceName = "";
                } else {
                    deviceName = device.getName();
                }
                String deviceHardwareAddress = device.getAddress(); // MAC addres
                synchronized (this) {
                    addresses.add(deviceHardwareAddress);
                }
            }
        }
    };
    public void die(){
        context.unregisterReceiver(receiver);
    }
}
