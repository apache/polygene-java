package org.qi4j.library.spatial.projections;

import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.library.spatial.SpatialRefSysManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jj on 17.11.14.
 */
public class ProjectionsTransformation {

    public  TGeometry transform(TGeometry geometry, CoordinateReferenceSystem targetCRS) throws IllegalCoordinateException, Exception
    {
        System.out.println("Transforming");

        CoordinateReferenceSystem sourceCRS = new ProjectionsRegistry().getCRS(geometry.getSRIDWkt());

        System.out.println(sourceCRS);

        TPoint point = (TPoint)geometry;

        double[] pointSource = transform((GeodeticCRS) sourceCRS, (GeodeticCRS) targetCRS, point.source());

        System.out.println("transformed points : " + Arrays.toString(pointSource));

        point.X(pointSource[0]);
        point.Y(pointSource[1]);
        point.setSRID(targetCRS.getAuthorityName(), Integer.parseInt(targetCRS.getAuthorityKey()));



        // CoordinateReferenceSystem sourceCRS = SpatialRefSysManager.getCRS(point.getSRIDWkt());

      //  point.coordinates().

        return point;
    }

// http://code.google.com/p/cloning
    public  TPoint transform(TPoint point, CoordinateReferenceSystem targetCRS) throws IllegalCoordinateException, Exception
    {
        System.out.println("Transforming");

        CoordinateReferenceSystem sourceCRS = new ProjectionsRegistry().getCRS(point.getSRIDWkt());

        System.out.println(sourceCRS);



        double[] pointSource = transform((GeodeticCRS) sourceCRS, (GeodeticCRS) targetCRS, point.source());

        System.out.println("transformed points : " + Arrays.toString(pointSource));

        point.X(pointSource[0]);
        point.Y(pointSource[1]);


        // CoordinateReferenceSystem sourceCRS = SpatialRefSysManager.getCRS(point.getSRIDWkt());

        //  point.coordinates().

        return point;
    }

//    public TGeometry transform(TGeometry geometry, CoordinateReferenceSystem targetCRS) throws IllegalCoordinateException {
/**
        if (sourceCRS.equals(targetCRS))
            return geometry;

        List<CoordinateOperation> ops;
        ops = CoordinateOperationFactory.createCoordinateOperations(sourceCRS, targetCRS);
        if (!ops.isEmpty()) {
            // if (verbose) {
            System.out.println(ops.get(0));
            // }
            // return ops.get(0).transform(new double[]{pointSource[0], pointSource[1], pointSource[2]});
            return null;
        } else {
            // return new double[]{0.0d, 0.0d, 0.0d};
            return null;
        }
 */
  //      return null;
  //  }

    public double[] transform(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, double[] pointSource) throws IllegalCoordinateException {

        if (sourceCRS.equals(targetCRS))
            return pointSource;

        List<CoordinateOperation> ops;
        ops = CoordinateOperationFactory.createCoordinateOperations(sourceCRS, targetCRS);
        if (!ops.isEmpty()) {
            if (true) {
            System.out.println(ops.get(0));
             }
            return ops.get(0).transform(new double[]{pointSource[0], pointSource[1], pointSource[2]});
        } else {
            return new double[]{0.0d, 0.0d, 0.0d};
        }
    }

/**
    public double[] transform(String csNameSrc, String csNameDest, double[] pointSource) throws IllegalCoordinateException, CRSException {

        CoordinateReferenceSystem inputCRS = cRSFactory.getCRS(csNameSrc);
        CoordinateReferenceSystem outputCRS = cRSFactory.getCRS(csNameDest);

        return transform((GeodeticCRS) inputCRS, (GeodeticCRS) outputCRS, pointSource);
    }
   */
}
