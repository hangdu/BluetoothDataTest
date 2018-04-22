package com.example.hang.bluetoothdatatest.PolyFitLib;

/**
 * Created by hang on 4/22/18.
 */

public class AverageFilter {
    int windowSize;
    double[] yArray;
    public AverageFilter(int size, double[] array) {
        windowSize = size;
        yArray = array;
    }

    public double[] filter() {
        double[] res = new double[yArray.length];
        for (int i = windowSize-1; i < res.length; i++) {
            //find res[i]
            double temp = 0;
            for (int j = i+1-windowSize; j <= i; j++) {
                temp += yArray[j];
            }
            res[i] = temp/windowSize;
        }

        for (int i = 0; i <  windowSize; i++) {
            res[i] = yArray[i];
        }
        return res;
    }
}