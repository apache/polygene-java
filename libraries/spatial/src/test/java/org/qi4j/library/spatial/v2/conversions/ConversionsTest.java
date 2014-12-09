package org.qi4j.library.spatial.v2.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;
import org.junit.Test;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.qi4j.api.geometry.TGEOM.TMULTIPOINT;
import static org.qi4j.api.geometry.TGEOM.TPOINT;
import static org.qi4j.library.spatial.v2.conversions.TConversions.*;

import java.util.Arrays;


/**
 * Created by jj on 04.12.14.
 */
public class ConversionsTest extends AbstractQi4jTest {

    private ObjectMapper GeoJsonMapper = new ObjectMapper();

    private final String CRS_EPSG_4326_ = "EPSG:4326";
    private final String CRS_EPSG_27572 = "EPSG:27572";

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);

        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS_EPSG_4326_);
    }

    @Test
    public void WhenConvertFromTGeometryToTGeometry() throws Exception
    {
        TPoint tPoint1 = TPOINT(module).x(11.57958981111).y(48.13905780941111).geometry();
        for (int i = 0;i < 1000000; i++) {
            TPoint tPoint2 = (TPoint) Convert(module).from(tPoint1).toTGeometry(CRS_EPSG_27572);
            TPoint tPoint3 = (TPoint) Convert(module).from(tPoint1).toTGeometry(CRS_EPSG_4326_);

        }
        // assertTrue(tPoint1.compareTo(tPoint2) == 0);
        System.out.println("Point " + tPoint1);
    }

    @Test
    public void WhenConvertPointFromGeoJsonToTGeometry()
    {
        TPoint tPoint1          = TPOINT(module).y(100).x(0).geometry();
        Point  geoJsonPoint1    = new Point(100, 0);
        TPoint tPoint2 = (TPoint)Convert(module).from(geoJsonPoint1).toTGeometry();
        assertTrue(tPoint1.compareTo(tPoint2) == 0);
    }

    @Test
    public void WhenConvertMultiPointFromGeoJsonToTGeometry()
    {
        TMultiPoint tMultiPoint1 = TMULTIPOINT(module).points(new double[][]
                {
                        {100d, 0d},
                        {101d, 1d},
                        {102d, 2d}

                }).geometry();

        MultiPoint geoJsonMultiPoint = new LineString(new LngLatAlt(100, 0), new LngLatAlt(101, 1), new LngLatAlt(102, 2));

        TMultiPoint tMultiPoint2 = (TMultiPoint)Convert(module).from(geoJsonMultiPoint).toTGeometry();
        System.out.println(tMultiPoint2);

        assertEquals(geoJsonMultiPoint.getCoordinates().size(),tMultiPoint2.getNumPoints());
        // TODO JJ - Compare further coordinates
    }

    @Test
    public void WhenConvertLineStringFromGeoJsonToTGeometry() throws Exception
    {
        LineString geoJsonLineString = GeoJsonMapper.readValue("{\"type\":\"LineString\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}",
                LineString.class);
        TLineString tLineString = (TLineString)Convert(module).from(geoJsonLineString).toTGeometry();
        // System.out.println(Convert(module).from(geoJsonLineString).toTGeometry());
    }

    @Test
    public void WhenConvertMultiLineStringFromGeoJsonToTGeometry() throws Exception
    {
        MultiLineString multiLineString = new MultiLineString();
        multiLineString.add(Arrays.asList(new LngLatAlt(100, 0), new LngLatAlt(101, 1)));
        multiLineString.add(Arrays.asList(new LngLatAlt(102, 2), new LngLatAlt(103, 3)));
    }

    @Test
    public void WhenConvertPolygonFromGeoJsonToTGeometry() throws Exception {

        Polygon polygon = GeoJsonMapper.readValue("{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],"
                + "[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]}", Polygon.class);
        TPolygon tPolygon = (TPolygon)Convert(module).from(polygon).toTGeometry();

        System.out.println(tPolygon);
    }
}
