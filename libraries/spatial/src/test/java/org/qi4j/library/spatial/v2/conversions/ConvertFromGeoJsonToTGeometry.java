package org.qi4j.library.spatial.v2.conversions;

import org.geojson.Point;
import org.junit.Test;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.library.spatial.v2.conversions.TConversions.Convert;

/**
 * Created by jj on 04.12.14.
 */
public class ConvertFromGeoJsonToTGeometry  extends AbstractQi4jTest {

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);

    }

    @Test
    public void WhenConvertingPoint()
    {

        Point point1  = new Point(100, 0);
        TPoint tPoint1 = (TPoint)Convert(module).from(point1).toTGeometry();
        Point point2  = (Point)Convert(module).from(point1).toGeoJson();
        System.out.println(point2.toString());
        TPoint tPoint2 = (TPoint)Convert(module).from(point2).toTGeometry();
        System.out.println(tPoint1);
        System.out.println(tPoint2);


        assertTrue(tPoint1.compareTo(tPoint2) == 0);
    }

    @Test
    public void WhenConvertingPoint2()
    {
        TPoint tPoint1 = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry();
        Point point1  = new Point(1,2 );
        Point point2  = (Point)Convert(module).from(point1).toGeoJson();
        System.out.println(point2.getCoordinates().getLatitude());
        System.out.println(point2.getCoordinates().getLongitude());
        TPoint tPoint2 = (TPoint)Convert(module).from(point2).toTGeometry();
        System.out.println(tPoint2);

    }
}
