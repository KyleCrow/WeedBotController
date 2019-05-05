package com.sti.weedbotcontroller.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Message;

import com.sti.weedbotcontroller.MainActivity;

import java.util.Set;

public class SearchThread extends Thread{
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothDevice WBdev = null;
    Set<BluetoothDevice> pairedDevices;

    public void run() {

        Message msgS = new Message();
        msgS.setTarget(MainActivity.handler);


            pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("WeedBot")) {
                        WBdev = device;
                        msgS.obj="sSucceed";
                        msgS.sendToTarget();
                        return;
                    }
                }
            }
        msgS.obj="sFailed";
        msgS.sendToTarget();


    }
}
