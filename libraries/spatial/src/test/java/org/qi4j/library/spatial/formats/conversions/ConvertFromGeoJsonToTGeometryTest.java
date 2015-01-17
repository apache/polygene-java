/*
 * Copyright 2014 Jiri Jetmar.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.spatial.formats.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.geometry.*;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGeometryFactory.*;


public class ConvertFromGeoJsonToTGeometryTest extends AbstractQi4jTest
{

    private final String CRS_EPSG_4326_ = "EPSG:4326";
    private final String CRS_EPSG_27572 = "EPSG:27572";
    private ObjectMapper geoJsonMapper = new ObjectMapper();

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        new TGeometryAssembler().assemble(module);
    }

    @Ignore("Benchmarking is not in scope for this test.")
    @Test
    public void whenConvertFromTGeometryToTGeometry() throws Exception
    {
        TPoint tPoint1 = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry();
        for (int i = 0; i < 1000000; i++)
        {
            TPoint tPoint2 = (TPoint) TConversions.Convert(module).from(tPoint1).toTGeometry(CRS_EPSG_27572);
            TPoint tPoint3 = (TPoint) TConversions.Convert(module).from(tPoint1).toTGeometry(CRS_EPSG_4326_);
        }
    }

    @Test
    public void whenConvertPointFromGeoJsonToTGeometry()
    {
        TPoint tPoint = TPoint(module).y(100).x(0).geometry();
        Point gPoint = new Point(100, 0);
        TPoint convTPoint = (TPoint) TConversions.Convert(module).from(gPoint).toTGeometry();
        assertTrue(tPoint.compareTo(convTPoint) == 0);
    }

    @Test
    public void whenConvertMultiPointFromGeoJsonToTGeometry()
    {
        TMultiPoint tMultiPoint = TMultiPoint(module).of
                (
                        TPoint(module).y(1).x(1).geometry(),
                        TPoint(module).y(2).x(2).geometry()
                ).geometry();
        MultiPoint multiPoint = new MultiPoint(new LngLatAlt(1, 1), new LngLatAlt(2, 2));
        TMultiPoint convTMultiPoint = (TMultiPoint) TConversions.Convert(module).from(multiPoint).toTGeometry();
        assertTrue(((TPoint) tMultiPoint.getGeometryN(0)).compareTo(convTMultiPoint.getGeometryN(0)) == 0);
    }


    @Test
    public void whenConvertLineStringFromGeoJsonToTGeometry() throws Exception
    {
        LineString lineString = geoJsonMapper.readValue("{\"type\":\"LineString\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}",
                LineString.class);
        TLineString convTLineString = (TLineString) TConversions.Convert(module).from(lineString).toTGeometry();
        assertTrue(TLineString(module).points(new double[][]{{100, 0}, {101, 1}}).geometry().compareTo(convTLineString) == 0);
    }

    @Test
    public void whenConvertMultiLineStringFromGeoJsonToTGeometry() throws Exception
    {
        MultiLineString multiLineString = new MultiLineString();
        multiLineString.add(Arrays.asList(new LngLatAlt(100, 0), new LngLatAlt(101, 1)));
        TMultiLineString convTMultiLineString = (TMultiLineString) TConversions.Convert(module).from(multiLineString).toTGeometry();
        TMultiLineString compTMultiLineString = TMultiLineString(module).of
                (
                        TLineString(module).points(new double[][]
                                {
                                        {0, 100},
                                        {1, 101}
                                }).geometry()
                ).geometry();
        assertTrue(((TLineString) compTMultiLineString.getGeometryN(0)).compareTo((TLineString) convTMultiLineString.getGeometryN(0)) == 0);
    }

    @Test
    public void whenConvertPolygonFromGeoJsonToTGeometry() throws Exception
    {
        Polygon polygon = geoJsonMapper.readValue("{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],"
                + "[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]}", Polygon.class);
        TPolygon convTPolygon = (TPolygon) TConversions.Convert(module).from(polygon).toTGeometry();

        TPolygon compTPolygon = TPolygon(module)
                .shell
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {0, 100},
                                                {0, 101},
                                                {1, 101},
                                                {1, 100},
                                                {0, 100}
                                        }).geometry()
                        )
                .withHoles
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {0.2, 100.2},
                                                {0.2, 100.8},
                                                {0.8, 100.8},
                                                {0.8, 100.2},
                                                {0.2, 100.2}
                                        }).geometry()
                        )
                .geometry();

        assertEquals(compTPolygon.getNumPoints(), convTPolygon.getNumPoints());
        assertTrue(compTPolygon.shell().get().compareTo(convTPolygon.shell().get()) == 0);
    }

    @Test
    public void whenConvertMultiPolygonFromGeoJsonToTGeometry() throws Exception
    {
        MultiPolygon multiPolygon = geoJsonMapper.readValue("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]],"
                + "[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],"
                + "[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]]}", MultiPolygon.class);
        TMultiPolygon convTMultiPolygon = (TMultiPolygon) TConversions.Convert(module).from(multiPolygon).toTGeometry();
        assertEquals(15, convTMultiPolygon.getNumPoints());
    }

    @Test
    public void whenConvertFeatureFromGeoJsonToTGeometry() throws Exception
    {
        Feature feature = new Feature();
        feature.setGeometry(new Point(100, 0));
        TFeature convTFeature = (TFeature) TConversions.Convert(module).from(feature).toTGeometry();
        TFeature compTFeature = TFeature(module).of(TPoint(module).y(100).x(0).geometry()).geometry();
        assertTrue(convTFeature.getNumPoints() == compTFeature.getNumPoints());
    }

    @Test
    public void whenConvertFeatureCollectionFromGeoJsonToTGeometry() throws Exception
    {
        Feature f1 = new Feature();
        f1.setGeometry(geoJsonMapper.readValue("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]],"
                + "[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],"
                + "[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]]}", MultiPolygon.class));

        Feature f2 = new Feature();
        f2.setGeometry(geoJsonMapper.readValue("{\"type\":\"LineString\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}",
                LineString.class));

        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.add(f1);
        featureCollection.add(f2);

        TFeatureCollection convTFeatureCollection = (TFeatureCollection) TConversions.Convert(module).from(featureCollection).toTGeometry();
    }

}
