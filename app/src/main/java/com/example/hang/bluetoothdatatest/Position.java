package com.example.hang.bluetoothdatatest;

/**
 * Created by hang on 2018/1/10.
 */
public class Position {
    private String label;
    private double RSSI;
    public Position(String label, double RSSI) {
        this.label = label;
        this.RSSI = RSSI;
    }

    public String getLabel() {
        return label;
    }

    public double getRSSI() {
        return RSSI;
    }
}
