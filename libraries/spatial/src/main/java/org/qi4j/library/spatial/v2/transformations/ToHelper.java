package org.qi4j.library.spatial.v2.transformations;

import org.cts.IllegalCoordinateException;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.v2.projections.SpatialRefSysManager;

import java.util.List;

/**
 * Created by jj on 04.12.14.
 */
public class ToHelper {

    private Module module;
    private TGeometry intermediate;
    private double maxPrecisionMeanConversionError = Double.MAX_VALUE;

    public ToHelper(Module module, TGeometry intermediate)
    {
        this.module = module;
        this.intermediate = intermediate;
    }

    private ToHelper() {}


    public  void to(String CRS, double maxPrecisionMeanConversionError) throws RuntimeException
    {
        this.maxPrecisionMeanConversionError = maxPrecisionMeanConversionError;
        to(CRS);
    }

    public  void to(String CRS) throws RuntimeException
    {
    try {
        GeodeticCRS sourceCRS = (GeodeticCRS) SpatialRefSysManager.getCRS(intermediate.getCRS());
        GeodeticCRS targetCRS = (GeodeticCRS) SpatialRefSysManager.getCRS(CRS);

        if (sourceCRS.equals(targetCRS)) {
            return;
        }
        switch (intermediate.getType()) {
            case POINT:
                transform(sourceCRS, targetCRS, new Coordinate[]{((TPoint) intermediate).getCoordinate()});
                break;
            case MULTIPOINT:
                transform(sourceCRS, targetCRS, ((TMultiPoint) intermediate).getCoordinates());
                break;
            case LINESTRING:
                transform(sourceCRS, targetCRS, ((TLineString) intermediate).getCoordinates());
                break;
            // case MULTILINESTRING    : transform(sourceCRS, targetCRS, (() intermediate).getCoordinates()); break; break;
            case POLYGON:
                transform(sourceCRS, targetCRS, ((TPolygon) intermediate).getCoordinates());
                break;
            case MULTIPOLYGON:
                transform(sourceCRS, targetCRS, ((TMultiPolygon) intermediate).getCoordinates());
                break;
            case FEATURE:
                transform(sourceCRS, targetCRS, ((TFeature) intermediate).getCoordinates());
                break;
            case FEATURECOLLECTION:
                transform(sourceCRS, targetCRS, ((TFeatureCollection) intermediate).getCoordinates());
                break;
        }

        // JJ TODO - Set nested TGeometries CRSs as well
        intermediate.setCRS(targetCRS.getCode());
    } catch(Exception _ex)
    {
        throw new RuntimeException(_ex);
    }
    }



    private void transformTPoint(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, TPoint tPoint ) throws Exception
    {
        transform(sourceCRS, targetCRS, new Coordinate[] { tPoint.getCoordinate() });
        tPoint.setCRS(targetCRS.getCode());
    }

    private void transformTMultiPoint(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, TMultiPoint tMultiPoint ) throws Exception
    {
        transform(sourceCRS, targetCRS, tMultiPoint.getCoordinates());
        tMultiPoint.setCRS(targetCRS.getCode());
        // tMultiPoint.
    }

    private void transformTLineString(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, TLineString tLineString ) throws Exception
    {

        transform(sourceCRS, targetCRS, tLineString.getCoordinates());
        tLineString.setCRS(targetCRS.getCode());
        // tMultiPoint.
    }


    private void transform(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, Coordinate... coordinates) throws IllegalCoordinateException
    {
        List<CoordinateOperation> ops;
        ops = CoordinateOperationFactory.createCoordinateOperations(sourceCRS, targetCRS);

        if (!ops.isEmpty()) {
            if (true) {
                 System.out.println("Number of Operations " + ops.size());
                 System.out.println("Precision " + ops.get(0).getPrecision() + " m.");
                 System.out.println(ops.get(0));
            }

            if (maxPrecisionMeanConversionError < Double.MAX_VALUE && maxPrecisionMeanConversionError < ops.get(0).getPrecision())
                throw new RuntimeException("Transformation from " + sourceCRS.getCode() + " to " + targetCRS.getCode() +
                        " can not be done with the requested precision of " + maxPrecisionMeanConversionError + " meters." +
                        " Current precision mean conversion error is " + ops.get(0).getPrecision() + " meters.");

            for (Coordinate coordinate : coordinates)
            {
                double[] c = ops.get(0).transform(new double[]{coordinate.x(), coordinate.y() /** z */} );
                coordinate.x(c[0]).y(c[1]);
            }

//        } else {
//            coordinate.x(0d).y(0d);

        }
    }



/**
    private double[] transform(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, double[] pointSource) throws IllegalCoordinateException {

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
*/



/**
 public double[] transform(String csNameSrc, String csNameDest, double[] pointSource) throws IllegalCoordinateException, CRSException {

 CoordinateReferenceSystem inputCRS = cRSFactory.getCRS(csNameSrc);
 CoordinateReferenceSystem outputCRS = cRSFactory.getCRS(csNameDest);

 return transform((GeodeticCRS) inputCRS, (GeodeticCRS) outputCRS, pointSource);
 }

 */

}
