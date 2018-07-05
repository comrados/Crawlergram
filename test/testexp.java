/*
 * Title: testexp.java
 * Project: telegramJ
 * Creator: mikriuko
 */

import topicextractor.maths.gaussnewton.GaussNewton;
import topicextractor.maths.gaussnewton.NoSquareException;

public class testexp {

    public static double func(double x, double a, double b){
        return a * Math.exp(b * x);
    }



    public static void main(String[] args) {
        /*
        double[] x = new double[51];
        double[] y = new double[51];
        for (int i=0; i<51; i++){
            x[i] = i;
            y[i] = func(i, 3, 0.1);
            if (i % 10 == 0){
                System.out.println(x[i] + " " + y[i]);
            }
        }

        double[] o = GaussNewton.gnoExpRegressInitVals(x, y);

        System.out.println(o[0] + " " + o[1]);
        */

        double[][] x = {{1}, {2}, {3}, {4}, {5}, {10}, {25}, {50}};
        double[] y = {15, 13, 8, 10, 10, 5.5, 1.2, 0.1};

        for (int i = 0; i < x.length; i++){
            System.out.println("F(x) = " + func(x[i][0], 2.50, 0.25));
        }

        GaussNewton gn = new GaussNewton() {
            @Override
            public double findY(double x, double[] b) {
                return b[0] * Math.exp(-b[1] * x);
            }
        };

        double[] b = new double[0];
        try {
            b = gn.optimise(x, y, 2);
        } catch (NoSquareException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < b.length; i++){
            System.out.println("b(" + i + ") = " + b[i]);
        }

    }


}
