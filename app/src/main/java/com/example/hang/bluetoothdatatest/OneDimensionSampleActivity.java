package com.example.hang.bluetoothdatatest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OneDimensionSampleActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("MessageByteArray");
            //messages.append(text + "\n");
            int RSSI = 0;
            int SNR = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                int v2;
                if (i == 1) {
                    //RSSI
                    v2 = b & 0xFF;
                    v2 = (256-v2)*(-1);
                    RSSI = v2;
                }  else if (i == 2) {
                    //SNR
                    v2 = b & 0xFF;
                    if (v2 > 127) {
                        v2 = (256-v2)*(-1);
                    }
                    SNR = v2;
                } else {
                    v2 = b & 0xFF;
                }
            }
            double revisedRSSI = RSSI;
            if (SNR < 0) {
                revisedRSSI = RSSI + 0.25 * SNR;
            }
            strengthList.add(revisedRSSI);
            textView.setText("RSSI="+RSSI+";SNR="+SNR+";revisedRSSI="+revisedRSSI);
        }
    };
    private BluetoothConnectionService bluetoothConnectionService;
    private Button btn_StartCollect;
    private Button btn_Stop;
    private Button btn_Process;
    private TextView textView;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable sendCommandRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] bytes = "2".getBytes(Charset.defaultCharset());
            bluetoothConnectionService.write(bytes);
        }
    };
    private ScheduledFuture<?> SendCommandHandler;
    private List<Double> strengthList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_dimension_sample);
        btn_StartCollect = (Button) findViewById(R.id.btn_StartCollect);
        btn_Stop = (Button) findViewById(R.id.btn_StopCollect);
        btn_Process = (Button) findViewById(R.id.btn_Process);
        textView = (TextView) findViewById(R.id.textView1);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        bluetoothConnectionService = BluetoothConnectionService.getInstance();
        strengthList = new ArrayList<>();

        btn_StartCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendCommandHandler = scheduler.scheduleAtFixedRate(sendCommandRunnable, 0, 2, TimeUnit.SECONDS);
            }
        });

        btn_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        SendCommandHandler.cancel(true);
                    }
                }, 0, TimeUnit.SECONDS);
                String str = listToStr();
                textView.setText("revisedRSSI list : " + str);
            }
        });

        btn_Process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private String listToStr() {
        StringBuilder builder = new StringBuilder();
        for (double strength : strengthList) {
            builder.append(strength + ";");
        }
        return builder.toString();
    }
}
