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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchModeActiviry extends AppCompatActivity {
    BluetoothConnectionService bluetoothConnectionService;
    final static String TAG= "SearchModeActiviry";
    private Button btn_addLabel;
    private Button btn_stop;
    private TextView textView;
    private Handler handler;
    private boolean sendAndreceiveData = false;
    private ListView listview_label;
    private List<Integer> tempRSSIlist;
    private List<Integer> tempSNRlist;
    private List<Integer> revisedRSSIlist;
    private PositionAdapter adapter;
    private ArrayList<Position> positionList;
    Set<String> labels;
    String curLabel = null;
    int count = 0;

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            count++;
            StringBuilder messages = new StringBuilder();
            messages.append("index = " + count + "\n");
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
                messages.append(v2 + " ");
            }
            if (sendAndreceiveData) {
                textView.setText(messages.toString());
                Log.d(TAG, "get RSSI = " + messages.toString());
                if ((bytes[0] & 0xFF) == 0x01) {
                    tempRSSIlist.add(RSSI);
                    tempSNRlist.add(SNR);

                    if (SNR > 0) {
                        revisedRSSIlist.add(RSSI);
                    } else {
                        revisedRSSIlist.add((int)(RSSI + 0.25 * SNR));
                    }
                }
            }
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
                        handler.sendEmptyMessageDelayed(1, 2000);
//                        if (sendAndreceiveData) {
//                            handler.sendEmptyMessageDelayed(1, 4000);
//                        }
                        break;
                    default:
                        break;
                }
            }
        };

        tempRSSIlist = new ArrayList<>();
        tempSNRlist = new ArrayList<>();
        revisedRSSIlist = new ArrayList<>();
        positionList = new ArrayList<>();
        labels = new HashSet<>();
        adapter = new PositionAdapter(this, positionList);

        bluetoothConnectionService = BluetoothConnectionService.getInstance();
        btn_addLabel = (Button) findViewById(R.id.btn_addLabel);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        textView = (TextView) findViewById(R.id.textView);
        listview_label = (ListView) findViewById(R.id.lv_label);
        listview_label.setAdapter(adapter);

        final View view = getLayoutInflater().inflate(R.layout.dialog_add_label, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Add a label");
        alertDialog.setCancelable(false);
        final EditText et_addLabel = (EditText) view.findViewById(R.id.etLabel);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                curLabel = et_addLabel.getText().toString();
                et_addLabel.setText("");
                if (labels.contains(curLabel)) {
                    Toast.makeText(SearchModeActiviry.this, "This label is already included!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "A new label is added as " + curLabel);
                sendAndreceiveData = true;
                //sent command 2 every 4 second.
//                Message msg = Message.obtain();
//                msg.what = 1;
//                handler.sendMessage(msg);
                handler.sendEmptyMessage(1);
                count = 0;
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
                sendAndreceiveData = false;
//                Log.d(TAG, "Stop button is clicked");
                handler.removeMessages(1);
                double aveRSSI = getAverage();

                StringBuilder s = new StringBuilder();
                s.append("RSSI:");
                for (int i = 0; i < tempRSSIlist.size(); i++) {
                    s.append(tempRSSIlist.get(i) + ",");
                }
                s.append("\n");
                s.append("SNR:");
                for (int i = 0; i < tempSNRlist.size(); i++) {
                    s.append(tempSNRlist.get(i) + ",");
                }

                s.append("\n");
                s.append("revised RSSI:");
                for (int i = 0; i < revisedRSSIlist.size(); i++) {
                    s.append(revisedRSSIlist.get(i) + ",");
                }
                textView.setText(s);
                tempRSSIlist.clear();
                tempSNRlist.clear();
                revisedRSSIlist.clear();

                labels.add(curLabel);

                Position pos = new Position(curLabel, aveRSSI);
                adapter.add(pos);
            }
        });
    }

    private double getAverage() {
        if (revisedRSSIlist.size() == 0) {
            return -1;
        }
        double sum = 0;
        for (int i = 0; i < revisedRSSIlist.size(); i++) {
            sum += revisedRSSIlist.get(i);
        }
        return sum / revisedRSSIlist.size();
    }
}
