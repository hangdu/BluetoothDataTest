package com.example.hang.bluetoothdatatest.PolyFitLib;

/**
 * Created by hang on 4/22/18.
 */

public class Expectation {
    double[] xArray;
    double[] yArray;
    public Expectation(double[] xArray, double[] yArray) {
        //the length of two arrays should be the same
        this.xArray = xArray;
        this.yArray= yArray;
    }

    double getExpectOfX() {
        double sum = 0;
        for (double temp : xArray) {
            sum += temp;
        }
        return sum/xArray.length;
    }

    double getExpectOfY() {
        double sum = 0;
        for (double temp : yArray) {
            sum += temp;
        }
        return sum/yArray.length;
    }

    double getExpectOfXY() {
        double sum = 0;
        for (int i = 0; i < xArray.length; i++) {
            double x = xArray[i];
            double y = yArray[i];
            sum += x*y;
        }
        return sum/yArray.length;
    }

    double getExpectOfX2() {
        double sum = 0;
        for (double temp : xArray) {
            sum += temp*temp;
        }
        return sum/xArray.length;
    }

    double getExpectOfX3() {
        double sum = 0;
        for (double temp : xArray) {
            sum += temp*temp*temp;
        }
        return sum/xArray.length;
    }

    double getExpectOfX4() {
        double sum = 0;
        for (double temp : xArray) {
            sum += temp*temp*temp*temp;
        }
        return sum/xArray.length;
    }

    double getExpectOfX2Y() {
        double sum = 0;
        for (int i = 0; i < xArray.length; i++) {
            double x = xArray[i];
            double y = yArray[i];
            sum += x*x*y;
        }
        return sum/yArray.length;
    }
}