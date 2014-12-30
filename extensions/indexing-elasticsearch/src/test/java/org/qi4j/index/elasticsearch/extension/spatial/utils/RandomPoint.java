package org.qi4j.index.elasticsearch.extension.spatial.utils;

import java.io.Serializable;


public class RandomPoint extends RandomUtils implements Serializable {

    /**
     * 	auto generated
     */
    private static final long serialVersionUID = -1772453101010620003L;

    /**
     *  for the case d = 2
     */
    private static final double twopi = 8*Math.atan(1);



    /**
     * Generates the next pseudorandom double value vector, with
     * equal probability of picking any vector on the unit
     * d-dimensional hypersphere.
     *
     * The algorithm is based on the work of Marsaglia in
     * Marsaglia, G. "Choosing a Point from the Surface of a Sphere."
     * Ann. Math. Stat. 43, 645-646, 1972
     *
     * and Muller in
     * Muller, M. E. "A Note on a Type for Generating Points Uniformly on N-Dimensional Spheres."
     * Comm. Assoc. Comput. Mach. 2, 19-20, Apr. 1959.
     *
     * See also:
     * Weisstein, Eric W. "Sphere Point Picking."
     * From MathWorld--A Wolfram Web Resource.
     * http://mathworld.wolfram.com/SpherePointPicking.html and
     * http://mathworld.wolfram.com/HyperspherePointPicking.html
     *
     * @return the next pseudorandom vector on the d-dimensional unit hypersphere
     */

    public RandomPoint(){
        this(System.currentTimeMillis());
    }

    public RandomPoint(long seed){
        setSeed(seed);
    }

    public double[] nextSpherePt(int d){

        double ret[] = new double[d];

        if (d == 1){
            ret[0] = -1;
            if(nextDouble() > 0.5)
                ret[0] = 1;
        }
        else if (d == 2){
            ret = new double[2];
            double theta = nextDouble()*twopi;
            ret[0] = Math.cos(theta);
            ret[1] = Math.sin(theta);
            return ret;
        }
        else if (d == 3){
            double x, y, z;
            ret = new double[3];
            x = 2*(nextDouble() - 0.5);
            y = 2*(nextDouble() - 0.5);
            while ((x*x + y*y) >= 1)
                y = 2*(nextDouble() - 0.5);
            z = 1 - 2*(x*x+y*y);
            ret[0] = x;
            ret[1] = y;
            ret[2] = z;
        }
        else{
            ret = new double[d];
            double nrm = 0;
            for (int jj = 0 ; jj < d ; jj ++){
                ret[jj] = nextGaussian();
                nrm += ret[jj]+ret[jj];
            }
            nrm = Math.sqrt(nrm);
            for (int jj = 0 ; jj < d ; jj ++){
                ret[jj] = ret[jj] / nrm;
            }
        }

        return ret;
    }

    /**
     * Generates the next pseudorandom double value vector, with
     * equal probability of picking any vector on the  d-dimensional
     * hypersphere of radius RR.
     *
     * The algorithm is based on the work of Marsaglia in
     * Marsaglia, G. "Choosing a Point from the Surface of a Sphere."
     * Ann. Math. Stat. 43, 645-646, 1972
     *
     * and Muller in
     * Muller, M. E. "A Note on a Type for Generating Points Uniformly on N-Dimensional Spheres."
     * Comm. Assoc. Comput. Mach. 2, 19-20, Apr. 1959.
     *
     * See also:
     * Weisstein, Eric W. "Sphere Point Picking."
     * From MathWorld--A Wolfram Web Resource.
     * http://mathworld.wolfram.com/SpherePointPicking.html and
     * http://mathworld.wolfram.com/HyperspherePointPicking.html
     *
     * @return the next pseudorandom vector on the d-dimensional hypersphere of radius RR.
     */

    public double[] nextSpherePt(int d, double RR){

        double ret[] = new double[d];

        if (d == 1){
            ret[0] = -1*RR;
            if(nextDouble() > 0.5)
                ret[0] = RR;
        }
        else if (d == 2){
            ret = new double[2];
            double theta = nextDouble()*twopi;
            ret[0] = RR*Math.cos(theta);
            ret[1] = RR*Math.sin(theta);
            return ret;
        }
        else{
            ret = new double[d];
            double nrm = 0;
            for (int jj = 0 ; jj < d ; jj ++){
                ret[jj] = nextGaussian();
                nrm += ret[jj]*ret[jj];
            }
            nrm = Math.sqrt(nrm);
            for (int jj = 0 ; jj < d ; jj ++){
                ret[jj] = RR*(ret[jj] / nrm);
            }
        }

        return ret;
    }

}
