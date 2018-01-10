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
 * Created by hang on 2018/1/10.
 */
public class PositionAdapter extends ArrayAdapter<Position> {
    public PositionAdapter(Context context, ArrayList<Position> positions) {
        super(context, 0, positions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Position curPos = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.position_item, parent, false);
        }

        // Lookup view for data population
        TextView tv_label = (TextView) convertView.findViewById(R.id.tv_label);
        TextView tv_RSSI = (TextView) convertView.findViewById(R.id.tv_RSSI);
        // Populate the data into the template view using the data object
        tv_label.setText(curPos.getLabel());
        tv_RSSI.setText(String.valueOf(curPos.getRSSI()));
        // Return the completed view to render on screen
        return convertView;
    }
}
