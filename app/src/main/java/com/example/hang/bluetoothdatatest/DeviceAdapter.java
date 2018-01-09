package com.example.hang.bluetoothdatatest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hang on 2018/1/8.
 */
public class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    public DeviceAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
        // Populate the data into the template view using the data object
        tvName.setText(device.getName());
        tvAddress.setText(device.getAddress());
        // Return the completed view to render on screen
        return convertView;
    }
}
