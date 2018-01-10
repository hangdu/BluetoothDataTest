package com.example.hang.bluetoothdatatest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.nio.charset.Charset;

/**
 * Created by hang on 2018/1/6.
 */
public class ScanModeActivity extends AppCompatActivity {
    private BluetoothConnectionService mBluetoothConnectionService;
    private TextView incomingMessage;
    private Handler handler;

    @Override
    protected void onDestroy() {
        handler.removeMessages(1);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_mode);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        incomingMessage  =(TextView) findViewById(R.id.textView);
        mBluetoothConnectionService = BluetoothConnectionService.getInstance();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case 1:
                        //send command 2
                        byte[] bytes = "2".getBytes(Charset.defaultCharset());
                        mBluetoothConnectionService.write(bytes);
                        handler.sendEmptyMessageDelayed(1, 4000);
                        break;
                    default:
                        break;
                }
            }
        };
        handler.sendEmptyMessage(1);

    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StringBuilder messages = new StringBuilder();
            byte[] bytes = intent.getByteArrayExtra("MessageByteArray");
            //messages.append(text + "\n");
            for (byte b : bytes) {
                int v2 = b & 0xFF; // v2 is 200 (0x000000C8)
                messages.append(v2 + " ");
            }
            incomingMessage.setText(messages.toString());

            if ((bytes[0] & 0xFF) == 1) {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            }
        }
    };
}
