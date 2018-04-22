package com.example.hang.bluetoothdatatest.PolyFitLib;

/**
 * Created by hang on 4/22/18.
 */

public class RSquared {
    double[] yArray;
    double[] yPredict;
    private double average;
    private double SS_tot;
    private double SS_reg;
    private double SS_res;
    int p;
    public RSquared(double[] yArray, double[] yPredict, int p) {
        this.yArray = yArray;
        this.yPredict = yPredict;
        this.p = p;
        average = getAverageY();
        SS_tot = getSS_tot();
        SS_reg = getSS_reg();
        SS_res = getSS_res();
    }

    private double getAverageY() {
        double sum = 0;
        for (double temp : yArray) {
            sum += temp;
        }
        return sum/yArray.length;
    }

    private double getSS_tot() {
        double sum = 0;
        for (int i = 0; i < yArray.length; i++) {
            double temp = (yArray[i]-average)*(yArray[i]-average);
            sum += temp;
        }
        return sum;
    }

    private double getSS_reg() {
        double sum = 0;
        for (int i = 0; i < yArray.length; i++) {
            double temp = (yPredict[i]-average)*(yPredict[i]-average);
            sum += temp;
        }
        return sum;
    }

    private double getSS_res() {
        double sum = 0;
        for (int i = 0; i < yArray.length; i++) {
            double temp = (yPredict[i]-yArray[i])*(yPredict[i]-yArray[i]);
            sum += temp;
        }
        return sum;
    }

    public double getRSquared() {
        return 1-SS_res/SS_tot;
    }

    public double getAdjustedRSquared() {
        int n = yArray.length;
        return 1-SS_res/SS_tot*(n-1)/(n-p);
    }
}
