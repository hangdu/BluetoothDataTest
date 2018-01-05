package com.example.hang.bluetoothdatatest;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hang on 2018/1/2.
 */
public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    ArrayList<BluetoothDevice> mBTDevices;
    private final Activity context;
    public DeviceListAdapter(Activity context, ArrayList<BluetoothDevice> mBTDevices) {
        super(context, R.layout.device_layout_view, mBTDevices);
        this.mBTDevices = mBTDevices;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.device_layout_view, null,true);
        TextView tvDeviceName = (TextView) rowView.findViewById(R.id.tvDeviceName);
        String name = mBTDevices.get(position).getName();
        tvDeviceName.setText(mBTDevices.get(position).getName());
        TextView tvDeviceAddress = (TextView) rowView.findViewById(R.id.tvDeviceAddress);

        String address = mBTDevices.get(position).getAddress();
        tvDeviceAddress.setText(mBTDevices.get(position).getAddress());
        return rowView;
    }
}
