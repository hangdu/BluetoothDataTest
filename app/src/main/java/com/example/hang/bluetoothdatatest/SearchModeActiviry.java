package com.example.hang.bluetoothdatatest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.Charset;

public class SearchModeActiviry extends AppCompatActivity {
    BluetoothConnectionService bluetoothConnectionService;
    final static String TAG= "SearchModeActiviry";
    private Button btn_addLabel;
    private Button btn_stop;
    private TextView textView;
    private Handler handler;
//    private boolean sendAndreceiveData = false;

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
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
            textView.setText(messages.toString());
            Log.d(TAG, "get RSSI = " + messages.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_mode_activiry);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case 1:
                        //send command 2
                        byte[] bytes = "2".getBytes(Charset.defaultCharset());
                        bluetoothConnectionService.write(bytes);
                        handler.sendEmptyMessageDelayed(1, 4000);
//                        if (sendAndreceiveData) {
//                            handler.sendEmptyMessageDelayed(1, 4000);
//                        }
                        break;
                    default:
                        break;
                }
            }
        };

        bluetoothConnectionService = BluetoothConnectionService.getInstance();
        btn_addLabel = (Button) findViewById(R.id.btn_addLabel);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        textView = (TextView) findViewById(R.id.textView);

        final View view = getLayoutInflater().inflate(R.layout.dialog_add_label, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Add a label");
        alertDialog.setCancelable(false);
        final EditText et_addLabel = (EditText) view.findViewById(R.id.etLabel);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String label = et_addLabel.getText().toString();
                Log.d(TAG, "A new label is added as " + label);
//                sendAndreceiveData = true;
                //sent command 2 every 4 second.
                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);

            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(view);
        btn_addLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sendAndreceiveData = false;
                handler.removeMessages(1);
            }
        });
    }
}
