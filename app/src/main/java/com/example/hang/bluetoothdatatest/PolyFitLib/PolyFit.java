package com.example.hang.bluetoothdatatest.PolyFitLib;

/**
 * Created by hang on 4/22/18.
 */

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;;
public class PolyFit {
    private double[] xArray;
    private double[] yArray;
    private Expectation expectation;

    private double expectofX;
    private double expectofY;
    private double expectofXY;
    private double expectofX2Y;
    private double expectofX2;
    private double expectofX3;
    private double expectofX4;

    public PolyFit(double[] yArray) {
        this.yArray = yArray;
        xArray = new double[yArray.length];
        for (int i = 0; i < yArray.length; i++) {
            xArray[i] = i+1;
        }
        expectation = new Expectation(xArray, this.yArray);
    }

    //must call init first before calling oneOrderFit and twoOrderFit functions
    public void init() {
        expectofX = expectation.getExpectOfX();
        expectofY = expectation.getExpectOfY();
        expectofXY = expectation.getExpectOfXY();
        expectofX2Y = expectation.getExpectOfX2Y();
        expectofX2 = expectation.getExpectOfX2();
        expectofX3 = expectation.getExpectOfX3();
        expectofX4 = expectation.getExpectOfX4();
    }

    public double[] oneOrderFit() {
        double[][] left = new double[][] { { expectofX, 1 }, {expectofX2, expectofX } };
        double[] right = new double[] {expectofY, expectofXY};
        RealMatrix coefficients =new Array2DRowRealMatrix(left, false);
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        RealVector constants = new ArrayRealVector(right, false);
        RealVector solution = solver.solve(constants);

        double a = solution.getEntry(0);
        double b = solution.getEntry(1);
        return new double[] {a, b};
    }

    public double[] twoOrderFit() {
        double[][] left = new double[][] { { expectofX2, expectofX, 1 }, { expectofX3, expectofX2, expectofX }, { expectofX4, expectofX3, expectofX2 } };
        double[] right = new double[] {expectofY, expectofXY, expectofX2Y};
        RealMatrix coefficients =new Array2DRowRealMatrix(left, false);
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        RealVector constants = new ArrayRealVector(right, false);
        RealVector solution = solver.solve(constants);

        double a = solution.getEntry(0);
        double b = solution.getEntry(1);
        double c = solution.getEntry(2);
        return new double[] {a,b,c};
    }
}