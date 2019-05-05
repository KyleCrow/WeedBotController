package com.sti.weedbotcontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sti.weedbotcontroller.bluetooth.ConnectThread;
import com.sti.weedbotcontroller.bluetooth.ConnectedThread;
import com.sti.weedbotcontroller.bluetooth.SearchThread;
import com.sti.weedbotcontroller.time.TimeThread;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static Handler handler;
    public static Snackbar snackbar;
    public static byte ConnectionState=0;
    public static ConnectThread connectThread;
    public static boolean motorState = false;
    public static boolean correctionState = false;
    public static boolean liquidState = false;
    public static boolean batteryState = false;
    public static int Time;
    public static int liquidQuantity = 0;
    public static Switch motorStateSwitch;
    public static Switch correctionStateSwitch;
    public static byte codes[] = {10, 11, 12, 13, 14, 15, 20, 21};
    static View view;

    public Toast toast;

    public FloatingActionButton fab;

    public Button btButton;

    public AppBarLayout appBar;

    public TextView btStatus;
    public TextView btText;
    public TextView timeText;
    public TextView liquidText;
    public TextView batteryText;
    public TextView liquidQText;
    public TextView scoreText;
    public TextView highScoreText;
    public TextView text1;
    public TextView text2;
    public TextView text3;
    public TextView text4;
    public TextView text5;
    public TextView text6;
    public TextView speedBarStatus;

    public ConstraintLayout btLayout;
    public ConstraintLayout controlLayout;

    public SeekBar speedBar;

    public int REQUEST_ENABLE_BT = 1;
    public int Score=0;
    public int Highscore=0;
    public int SeekbarValue;

    byte isConnecting=0;
    byte speedTransmit[] = new byte[3];

    SearchThread searchThread;
    ConnectedThread connectedThread;
    TimeThread timeThread;

    Map messageCodes = new HashMap();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btLayout = findViewById(R.id.BtLayout);
        controlLayout = findViewById(R.id.ControlLayout);
        fab = findViewById(R.id.fab);
        appBar = findViewById(R.id.appBar);
        btStatus = findViewById(R.id.BtStatus);
        view = findViewById(android.R.id.content);
        snackbar.make(view, "", Snackbar.LENGTH_SHORT);
        speedBar = findViewById(R.id.SpeedBar);
        speedBarStatus = findViewById(R.id.SpeedBarStatus);
        btText = findViewById(R.id.BtText);
        timeText = findViewById(R.id.TimeText);
        liquidText = findViewById(R.id.LiquidText);
        batteryText = findViewById(R.id.BatteryText);
        liquidQText = findViewById(R.id.LiquidQText);
        scoreText = findViewById(R.id.ScoreText);
        highScoreText = findViewById(R.id.HighScoreText);
        text1 = findViewById(R.id.Text1);
        text2 = findViewById(R.id.Text2);
        text3 = findViewById(R.id.Text3);
        text4 = findViewById(R.id.Text4);
        text5 = findViewById(R.id.Text5);
        text6 = findViewById(R.id.Text6);
        btButton = findViewById(R.id.BtButton);
        btButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        });
        motorStateSwitch = findViewById(R.id.MotorStateSwitch);
        motorStateSwitch.setEnabled(false);
        motorStateSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.write((byte)20);
                motorState=!motorState;
            }
        });
        correctionStateSwitch = findViewById(R.id.CorrectionStateSwitch);
        correctionStateSwitch.setEnabled(false);
        correctionStateSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.write((byte)21);
                correctionState=!correctionState;
                speedBar.setEnabled(!correctionState);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (ConnectionState) {
                    case 0:
                        if (isConnecting == 0) {
                            isConnecting = 1;
                            snack("Connexion à WeedBot...", Snackbar.LENGTH_INDEFINITE);
                            searchThread = new SearchThread();
                            searchThread.start();
                        }
                        break;
                    case 1:
                        connectedThread.cancel();
                        connectThread.cancel();
                        timeThread.stop=true;
                        break;
                }
            }
        });
        speedTransmit[0]=22;
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedBarStatus.setText(Integer.toString(progress+1));
                SeekbarValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speedTransmit[1] = (byte)(SeekbarValue);
                connectedThread.writeLong(speedTransmit);
            }
        });

        //BT Layout
        if (ConnectThread.bluetoothAdapter == null) {
            btButton.setVisibility(View.INVISIBLE);
            controlLayout.setVisibility(View.INVISIBLE);
            btText.setText("Votre appareil ne possède pas de capteur bluetooth :(");
        } else {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter);
            //Bluetooth desactive
            if (!ConnectThread.bluetoothAdapter.isEnabled()) {
                btLayout.setVisibility(View.VISIBLE);
                controlLayout.setVisibility(View.INVISIBLE);
                fab.setEnabled(false);

            } else { //sinon
                btLayout.setVisibility(View.INVISIBLE);
                controlLayout.setVisibility(View.VISIBLE);
                fab.setEnabled(true);
            }
        }


        // MAP
        messageCodes.put("sSucceed", 1);
        messageCodes.put("sFailed", 2);
        messageCodes.put("cSucceed", 3);
        messageCodes.put("cRevoked", 4);
        messageCodes.put("cFailed", 5);
        messageCodes.put("updateSwitches", 6);
        messageCodes.put("updateLText", 7);
        messageCodes.put("updateBText", 8);
        messageCodes.put("updateSText", 9);
        messageCodes.put("updateTime", 10);

        DisconnectedUI();


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (messageCodes.containsKey(msg.obj)) {
                    int errCode =Integer.parseInt(messageCodes.get(msg.obj).toString());
                    switch (errCode) {
                        case 1:
                            //TimeThread tthread = new TimeThread();
                            //tthread.start();
                            connectThread = new ConnectThread(SearchThread.WBdev);
                            connectThread.start();
                            break;
                        case 2:
                            isConnecting=0;
                            dismissSnack();
                            snack("Veuillez Appairer WeedBot", Snackbar.LENGTH_SHORT);
                            break;
                        case 3:
                            ConnectionState=1;
                            isConnecting=0;
                            connectedThread = new ConnectedThread(connectThread.socket());
                            connectedThread.start();
                            ConnectedUI();dismissSnack();
                            dismissToast();
                            toast("La connexion à WeedBot a réussi", Toast.LENGTH_SHORT);
                            connectedThread.write(codes[0]);
                            connectedThread.write(codes[1]);
                            connectedThread.write(codes[2]);
                            connectedThread.write(codes[3]);
                            connectedThread.write(codes[4]);
                            connectedThread.write(codes[5]);
                            timeThread = new TimeThread();
                            timeThread.start();
                            break;
                        case 4:
                            ConnectionState=0;
                            isConnecting=0;
                            dismissToast();
                            toast("Connexion révoquée", Toast.LENGTH_SHORT);
                            DisconnectedUI();
                            break;
                        case 5:
                            ConnectionState=0;
                            isConnecting=0;
                            dismissSnack();
                            dismissToast();
                            toast("La connexion à WeedBot a échoué", Toast.LENGTH_SHORT);
                            DisconnectedUI();
                            break;

                        case 6:
                            motorStateSwitch.setChecked(motorState);
                            correctionStateSwitch.setChecked(correctionState);
                            speedBar.setEnabled(!correctionState);
                            speedBarStatus.setText(Integer.toString(speedBar.getProgress()));
                            break;

                        case 7:
                            if (liquidState) {
                                liquidText.setText(R.string.LiquidHigh);
                                liquidText.setTextColor(getResources().getColor(R.color.colorGreen));
                            }   else {
                                liquidText.setText(R.string.LiquidLow);
                                liquidText.setTextColor(getResources().getColor(R.color.colorRed));
                            }
                            break;

                        case 8:
                            if (batteryState) {
                                batteryText.setText(R.string.BatteryHigh);
                                batteryText.setTextColor(getResources().getColor(R.color.colorGreen));
                            }   else {
                                batteryText.setText(R.string.BatteryLow);
                                batteryText.setTextColor(getResources().getColor(R.color.colorRed));
                            }
                            break;

                        case 9:
                            Score=liquidQuantity/24;
                            liquidQText.setText(Integer.toString(liquidQuantity));
                            scoreText.setText(Integer.toString(Score));
                            if(Score>Highscore) {
                                Highscore=Score;
                                highScoreText.setText(Integer.toString(Highscore));
                            }
                            break;
                        case 10:
                            if (ConnectionState==1) {
                                Time = Time + 1;
                                if (Time >= 3600) {
                                    timeText.setText(Time / 3600 + " h " + ((Time-((Time/3600)*3600))/60) * 60 + " min " + (Time-((Time/60)*60)) * 60 + " sec");
                                } else if (Time >= 60) {
                                    timeText.setText(Time / 60 + " min " + (Time-((Time/60)*60)) + " sec");
                                } else {
                                    timeText.setText(Time + " sec");
                                }
                            }

                            break;

                    }
                }   else {

                }
            }
        };

    }
     public static void snack(String text, int duration) {
        snackbar = snackbar.make(view, text, duration);
        snackbar.show();
     }

     public static void dismissSnack(){
        if (snackbar != null) {
            snackbar.dismiss();
        }
     }


    public void toast(String text, int duration) {
        toast = toast.makeText(MainActivity.this, text, duration);
        toast.show();
    }

    public void dismissToast(){
        if (toast != null) {
            toast.cancel();
        }
    }

     private void ConnectedUI() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
         }
        motorStateSwitch.setEnabled(true);
        correctionStateSwitch.setEnabled(true);
        text1.setTextColor(getResources().getColor(R.color.colorTextEnabled));
        text2.setTextColor(getResources().getColor(R.color.colorTextEnabled));
        text3.setTextColor(getResources().getColor(R.color.colorTextEnabled));
        text3.setTextColor(getResources().getColor(R.color.colorTextEnabled));
        text4.setTextColor(getResources().getColor(R.color.colorTextEnabled));
        text5.setTextColor(getResources().getColor(R.color.colorTextEnabled));
        text6.setTextColor(getResources().getColor(R.color.colorTextEnabled));

        appBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btStatus.setText(R.string.btStatusOn);
     }

     private void DisconnectedUI() {

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             getWindow().setStatusBarColor(getResources().getColor(R.color.colorRedDark));
         }
         motorStateSwitch.setEnabled(false);
         motorStateSwitch.setChecked(false);
         correctionStateSwitch.setEnabled(false);
         correctionStateSwitch.setChecked(false);
         speedBar.setEnabled(false);
         speedBar.setProgress(145);
         text1.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         text2.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         text3.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         text3.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         text4.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         text5.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         text6.setTextColor(getResources().getColor(R.color.colorTextDisabled));
         timeText.setText(R.string.nullText);
         liquidText.setText(R.string.nullText);
         batteryText.setText(R.string.nullText);
         liquidQText.setText(R.string.nullText);
         scoreText.setText(R.string.nullText);
         highScoreText.setText(R.string.nullText);
         speedBarStatus.setText(R.string.nullText);
         appBar.setBackgroundColor(getResources().getColor(R.color.colorRed));
         btStatus.setText(R.string.btStatusOff);
     }




    //BROADCAST RECEIVER
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        btLayout.setVisibility(View.VISIBLE);
                        controlLayout.setVisibility(View.INVISIBLE);
                        fab.setEnabled(false);
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        btButton.setText("Activer le Bluetooth");

                        break;
                    case BluetoothAdapter.STATE_ON:
                        btLayout.setVisibility(View.INVISIBLE);
                        controlLayout.setVisibility(View.VISIBLE);
                        fab.setEnabled(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        btButton.setText("Activation du Bluetooth...");
                        break;
                }
            }
        }
    };
    //DESTROY
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

    }
}