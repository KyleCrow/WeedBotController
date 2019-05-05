package com.sti.weedbotcontroller.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sti.weedbotcontroller.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ConnectedThread extends Thread {
    private Handler handler;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer= new byte[1];
    private byte[] Transmission = new byte[4];
    private Boolean isTransmitting=false;
    private int counter=0;

    private int t1=0;
    private int t2=0;
    private int t3=0;

    private interface MessageConstants {
        int MESSAGE_TOAST = 2;

    }



        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            while (true) {
                try {
                    mmInStream.read(mmBuffer);
                    if (mmBuffer[0] == 0){
                        isTransmitting = !isTransmitting;
                        counter = 0;
                        if (!isTransmitting) {
                            switch (Transmission[0]) {
                                case 10:
                                    if (Transmission[1]<0) {
                                        t1 = (Transmission[1]&0x7F)+128;
                                    } else {
                                        t1=Transmission[1];
                                    }
                                    if (Transmission[2] < 0) {
                                        t2 = ((Transmission[2] & 0x7F) << 8) + 32768;
                                    } else {
                                        t2 = (Transmission[2]<<8);
                                    }
                                    if (Transmission[3] < 0) {
                                        t3 = ((Transmission[1] & 0x7F) << 16) + 8388608;
                                    } else {
                                        t3 = (Transmission[3]<<16);
                                    }
                                    MainActivity.Time=t1+t2+t3;
                                    break;
                                case 11:
                                    MainActivity.motorState = byteToBool(Transmission[1]);
                                    Message msgms = new Message();
                                    msgms.obj = "updateSwitches";
                                    msgms.setTarget(MainActivity.handler);
                                    msgms.sendToTarget();
                                    break;
                                case 12:
                                    MainActivity.correctionState = byteToBool(Transmission[1]);
                                    Message msgcs = new Message();
                                    msgcs.obj = "updateSwitches";
                                    msgcs.setTarget(MainActivity.handler);
                                    msgcs.sendToTarget();
                                    break;

                                case 13:
                                    MainActivity.liquidState = byteToBool(Transmission[1]);
                                    Message msgls = new Message();
                                    msgls.obj = "updateLText";
                                    msgls.setTarget(MainActivity.handler);
                                    msgls.sendToTarget();
                                    break;

                                case 14:
                                    MainActivity.batteryState = byteToBool(Transmission[1]);
                                    Message msgbs = new Message();
                                    msgbs.obj = "updateBText";
                                    msgbs.setTarget(MainActivity.handler);
                                    msgbs.sendToTarget();
                                    break;

                                case 15:
                                    MainActivity.liquidQuantity = Transmission[1]-1;
                                    Message msglq = new Message();
                                    msglq.obj = "updateSText";
                                    msglq.setTarget(MainActivity.handler);
                                    msglq.sendToTarget();
                                    break;
                            }
                            for (int count=0; count<3;count++) {
                                Transmission[count]=0;
                            }

                        }
                    }
                    if (isTransmitting && mmBuffer[0] != 0) {
                        Transmission[counter] = mmBuffer[0];
                        counter = counter+1;
                    }

                } catch (IOException e) {
                    Log.d("ConnectedThread", "Input stream was disconnected", e);
                    cancel();
                    break;
                }
            }
        }

        public void write(byte bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error occurred when sending data", e);

                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Les données n'ont pas pu être envoyées");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

    public void writeLong(byte bytes[]) {
        try {
            mmOutStream.write((byte)255);
            mmOutStream.write(bytes[0]);
            mmOutStream.write(bytes[1]);
            mmOutStream.write((byte)255);

        } catch (IOException e) {
            Log.e("ConnectedThread", "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
        }
    }

    // /!\ Attention cette fonction convertit les valeurs envoyées par le programme Arduino Weedbot!
    // /!\ Elle ne supporte pas les valeurs booleennes universelles.
    public static boolean byteToBool(byte i) {
        if (i==2) {
            return true;
        } else {
            return false;
        }
    }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Could not close the connect socket", e);
            }
        }
}
