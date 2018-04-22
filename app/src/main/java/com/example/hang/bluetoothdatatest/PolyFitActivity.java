package com.example.hang.bluetoothdatatest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.hang.bluetoothdatatest.PolyFitLib.AverageFilter;
import com.example.hang.bluetoothdatatest.PolyFitLib.PolyFit;
import com.example.hang.bluetoothdatatest.PolyFitLib.RSquared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolyFitActivity extends AppCompatActivity {
    private XYPlot plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poly_fit);

        plot = (XYPlot) findViewById(R.id.plot);

        Intent intent = getIntent();
        ArrayList<Double> list = (ArrayList<Double>)intent.getSerializableExtra("array");
//        double[] original = {-65,-53,-56,-53,-60,-55,-60,-48,-49,-53,-57,-58,-49,-40,-39,-40,-34,-50,-36,-30,-38,-44,-33,-39,-48,-50,-56,-48,-64,-46,-48,-66,-60,-60,-56,
//                -56,-61,-64,-63,-63,-67,-64,-76,-68,-67,-66,-62};
        double[] original = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            original[i] = list.get(i);
        }
        double[] filtered = new AverageFilter(2, original).filter();
        int len = original.length;
        // create a couple arrays of y-values to plot:
        Number[] series1Numbers = getSeriesNumbers(original);
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Or");
        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(this, R.xml.point_formatter);
        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        Number[] series2Numbers = getSeriesNumbers(filtered);
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "F");
        LineAndPointFormatter series2Format = new LineAndPointFormatter(this, R.xml.line_point_formatter_purple);
        series2Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        plot.addSeries(series2, series2Format);


        PolyFit polyFit = new PolyFit(filtered);
        polyFit.init();
        double[] oneOrderFitRes = polyFit.oneOrderFit();

        LineAndPointFormatter series3Format = new LineAndPointFormatter(this, R.xml.line_point_formatter_red);
        series3Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        XYSeries series3 = generateSeries(0, len-1, len*10, 1, oneOrderFitRes[0], oneOrderFitRes[1], 0);
        plot.addSeries(series3, series3Format);

        //get y_predicted
        double[] yOneOrder_predicted = new double[filtered.length];
        for (int i = 0; i < len; i++) {
            yOneOrder_predicted[i] = OneOrderfx(i, oneOrderFitRes[0], oneOrderFitRes[1]);
        }
        RSquared rSuqred1 = new RSquared(filtered, yOneOrder_predicted, 2);
        double rsquared = rSuqred1.getRSquared();
        double adrsquared = rSuqred1.getAdjustedRSquared();



        double[] twoOrderFitRes = polyFit.twoOrderFit();
        LineAndPointFormatter series4Format = new LineAndPointFormatter(this, R.xml.line_point_formatter_green);
        series4Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
        XYSeries series4 = generateSeries(0, len-1, len*10, 2, twoOrderFitRes[0], twoOrderFitRes[1], twoOrderFitRes[2]);
        plot.addSeries(series4, series4Format);
        double[] yTwoOrder_predicted = new double[filtered.length];
        for (int i = 0;i < len; i++) {
            yTwoOrder_predicted[i] = TwoOrderfx(i, twoOrderFitRes[0], twoOrderFitRes[1], twoOrderFitRes[2]);
        }
        RSquared rSuqred2 = new RSquared(filtered, yTwoOrder_predicted, 3);
        double rsquared2 = rSuqred2.getRSquared();
        double adjustedR2 = rSuqred2.getAdjustedRSquared();
    }

    Number[] getSeriesNumbers(double[] array) {
        Number[] nums = new Number[array.length];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = array[i];
        }
        return nums;
    }

    protected XYSeries generateSeries(double minX, double maxX, double resolution, int order, double a, double b, double c) {
        //the value of order only has two choices: 1 or 2. Otherwise, throw exception
        final double range = maxX - minX;
        final double step = range / resolution;
        List<Number> xVals = new ArrayList<>();
        List<Number> yVals = new ArrayList<>();
        if (order == 1) {
            double x = minX;
            while (x <= maxX) {
                xVals.add(x);
                yVals.add(OneOrderfx(x, a, b));
                x +=step;
            }
            return new SimpleXYSeries(xVals, yVals, "One");
        } else {
            double x = minX;
            while (x <= maxX) {
                xVals.add(x);
                yVals.add(TwoOrderfx(x, a, b, c));
                x +=step;
            }
            return new SimpleXYSeries(xVals, yVals, "Two");
        }
    }

    protected double TwoOrderfx(double x, double a, double b, double c) {
        return a*Math.abs(x*x) + b*x + c;
    }

    protected double OneOrderfx(double x, double a, double b) {
        return a*x + b;
    }

    double[] getYValules(XYSeries series) {
        double[] yVals = new double[series.size()];
        for (int i = 0; i <  yVals.length; i++) {
            yVals[i] = series.getY(i).doubleValue();
        }
        return yVals;
    }

}
