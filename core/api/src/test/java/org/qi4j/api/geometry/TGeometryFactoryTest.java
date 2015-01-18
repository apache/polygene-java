/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.geometry;

import org.junit.Test;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.api.geometry.TGeometryFactory.TFeature;
import static org.qi4j.api.geometry.TGeometryFactory.TFeatureCollection;
import static org.qi4j.api.geometry.TGeometryFactory.TLineString;
import static org.qi4j.api.geometry.TGeometryFactory.TMultiLineString;
import static org.qi4j.api.geometry.TGeometryFactory.TMultiPoint;
import static org.qi4j.api.geometry.TGeometryFactory.TMultiPolygon;
import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.api.geometry.TGeometryFactory.TPolygon;


public class TGeometryFactoryTest
        extends AbstractQi4jTest
{

    private final double ZERO = 0d;
    private final String CRS_EPSG_27572 = "EPSG:27572";

    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {

        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);
        // API values
        module.values(
                TCRS.class,
                TPoint.class,
                TMultiPoint.class,
                TLineString.class,
                TMultiLineString.class,
                TPolygon.class,
                TMultiPolygon.class,
                TFeature.class,
                TFeatureCollection.class);

        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS_EPSG_27572);
    }

    @Test
    public void script01()
    {
        String CRS_EPSG_4326 = "EPSG:4326";
        TCRS crs = TCrs(module).crs(CRS_EPSG_4326);
        assertThat(crs.crs(), equalTo(CRS_EPSG_4326));
    }

    @Test
    public void script02()
    {
        TPoint point_2D = TPoint(module).x(1d).y(2d).geometry();
        assertEquals(1d, point_2D.x(), ZERO);
        assertEquals(2d, point_2D.y(), ZERO);
        assertTrue(point_2D.getCoordinates().length == 1);
        assertThat(point_2D.getCRS(), equalTo(CRS_EPSG_27572));


        TPoint point_3D = TPoint(module).x(1d).y(2d).z(3d).geometry();
        assertEquals(1d, point_3D.x(), ZERO);
        assertEquals(2d, point_3D.y(), ZERO);
        assertEquals(3d, point_3D.z(), ZERO);
        assertTrue(point_3D.getCoordinates().length == 1);
        assertThat(point_3D.getCRS(), equalTo(CRS_EPSG_27572));
    }

    @Test
    public void script03()
    {

        TMultiPoint multiPoint = TMultiPoint(module).points(new double[][]
                {
                        {0d, 0d},
                        {1d, 1d},

                }).geometry();

        assertTrue(multiPoint.getNumGeometries() == 2);
        assertEquals(multiPoint.getType(), TGeometry.TGEOMETRY_TYPE.MULTIPOINT);
        assertEquals(multiPoint.getGeometryN(0).getType(), TGeometry.TGEOMETRY_TYPE.POINT);
        assertTrue(multiPoint.getCoordinates().length == 2);
        assertThat(multiPoint.getCRS(), equalTo(CRS_EPSG_27572));


        assertEquals(0d, ((TPoint) multiPoint.getGeometryN(0)).x(), ZERO);
        assertEquals(0d, ((TPoint) multiPoint.getGeometryN(0)).y(), ZERO);
        assertThat((multiPoint.getGeometryN(0)).getCRS(), equalTo(CRS_EPSG_27572));


        assertEquals(1d, ((TPoint) multiPoint.getGeometryN(1)).x(), ZERO);
        assertEquals(1d, ((TPoint) multiPoint.getGeometryN(1)).y(), ZERO);

        // test DSL
        multiPoint.yx(2d, 2d);

        assertTrue(multiPoint.getNumGeometries() == 3);
        assertEquals(2d, ((TPoint) multiPoint.getGeometryN(2)).x(), ZERO);
        assertEquals(2d, ((TPoint) multiPoint.getGeometryN(2)).y(), ZERO);

        // overwrite & compare
        assertEquals(3d, ((TPoint) multiPoint.getGeometryN(2)).x(3).x(), ZERO);
        assertEquals(3d, ((TPoint) multiPoint.getGeometryN(2)).y(3).y(), ZERO);
    }


    @Test
    public void script04()
    {

        TLineString line = TLineString(module).points(new double[][]
                {
                        {0, 0},
                        {1, 0},
                        {1, 1},
                        {0, 1},
                        {0, 1}

                }).geometry();

        assertThat(line.getCRS(), equalTo(CRS_EPSG_27572));
        assertFalse(line.isEmpty());
        assertFalse(line.isRing()); // is not a ring
        assertFalse(line.isClosed());
        assertTrue(line.getCoordinates().length == 5);

    }

    @Test
    public void script05()
    {
        TMultiLineString mLine = TMultiLineString(module).of
                (
                        TLineString(module).points(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 1}

                                }).geometry()
                ).geometry();

        assertThat(mLine.getCRS(), equalTo(CRS_EPSG_27572));
        assertEquals(5, mLine.getNumPoints());
        assertTrue(mLine.getGeometryN(0).isLineString());
    }


    @Test
    public void script06()
    {

        TLinearRing ring = TLinearRing(module).ring(new double[][]
                {
                        {0, 0},
                        {1, 0},
                        {1, 1},
                        {0, 1},
                        {0, 0}
                }).geometry();

        assertThat(ring.getCRS(), equalTo(CRS_EPSG_27572));
        assertFalse(ring.isEmpty());
        assertTrue(ring.isRing());
        assertTrue(ring.isClosed());
        assertTrue(ring.getCoordinates().length == 5);
    }

    @Test
    public void script07()
    {
        TLinearRing tLinearRing = TLinearRing(module).of().geometry();
    }

    @Test
    public void script08()
    {
        TPolygon polygon = TPolygon(module)

                .shell
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {0, 0},
                                                {1, 0},
                                                {1, 1},
                                                {0, 1},
                                                {0, 0}

                                        }).geometry()
                        )

                .withHoles
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {0, 0},
                                                {1, 0},
                                                {1, 1},
                                                {0, 1},
                                                {0, 0}

                                        }).geometry()
                        )
                .geometry();

        assertTrue(polygon.getNumPoints() == 10);
        assertTrue(polygon.getCoordinates().length == 10);
    }

    @Test
    public void script09()
    {
        TPolygon polygon = TPolygon(module)

                .shell
                        (new double[][]
                                        {
                                                {0, 0},
                                                {1, 0},
                                                {1, 1},
                                                {0, 1},
                                                {0, 0}
                                        }


                        ).geometry();


        assertTrue(polygon.getNumPoints() == 5);
        assertTrue(polygon.getCoordinates().length == 5);
        assertEquals(0d, polygon.shell().get().getPointN(0).x(), ZERO);
        assertEquals(1d, polygon.shell().get().getPointN(1).y(1).y(), ZERO); // test dsl
        assertThat(polygon.getCRS(), equalTo(CRS_EPSG_27572));
        assertThat(polygon.shell().get().getCRS(), equalTo(CRS_EPSG_27572));
        assertThat(polygon.shell().get().getPointN(0).getCRS(), equalTo(CRS_EPSG_27572));

        // invalidate ring
        assertTrue(polygon.shell().get().isValid());
        assertEquals(1d, polygon.shell().get().getPointN(0).y(1).y(), ZERO);
        assertFalse(polygon.shell().get().isValid());
    }

    @Test
    public void script11()
    {
        TPolygon polygon = TPolygon(module)

                .shell
                        (
                                new double[][]
                                        {
                                                {0, 0},
                                                {3, 0},
                                                {1, 3},
                                                {0, 3},
                                                {0, 0}
                                        }
                        )
                .withHoles(

                        TLinearRing(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 0}

                                }).geometry()

                        , TLinearRing(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {2, 0},
                                        {2, 2},
                                        {0, 2},
                                        {0, 0}

                                }).geometry()
                )
                .geometry();

        assertTrue(polygon.getNumPoints() == 15);
        assertTrue(polygon.getCoordinates().length == 15);
        assertThat(polygon.getCRS(), equalTo(CRS_EPSG_27572));
    }


    @Test
    public void script12()
    {

        TPolygon polygon1 = TPolygon(module)

                .shell
                        (
                                new double[][]
                                        {
                                                {0, 0},
                                                {4, 0},
                                                {0, 4},
                                                {0, 4},
                                                {0, 0}
                                        }


                        )
                .withHoles(

                        TLinearRing(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 0}

                                }).geometry()

                        , TLinearRing(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {2, 0},
                                        {2, 2},
                                        {0, 2},
                                        {0, 0}

                                }).geometry()


                )
                .geometry();

        TPolygon polygon2 = TPolygon(module)

                .shell
                        (
                                new double[][]
                                        {
                                                {0, 0},
                                                {3, 0},
                                                {1, 3},
                                                {0, 3},
                                                {0, 0}
                                        }


                        )
                .withHoles(

                        TLinearRing(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 0}

                                }).geometry()

                        , TLinearRing(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {2, 0},
                                        {2, 2},
                                        {0, 2},
                                        {0, 0}

                                }).geometry()


                )
                .geometry();

        TMultiPolygon multiPolygon = TMultiPolygon(module).of(polygon1, polygon2).geometry();
        assertTrue(multiPolygon.getNumGeometries() == 2);
        assertTrue(multiPolygon.getCoordinates().length == 30);
        assertThat(multiPolygon.getCRS(), equalTo(CRS_EPSG_27572));
    }

    @Test
    public void script13()
    {

        TFeature featureOfAPoint = TFeature(module).of(TPoint(module).x(1d).y(2d).z(3d).geometry()).geometry();
        assertEquals(featureOfAPoint.getType(), TGeometry.TGEOMETRY_TYPE.FEATURE);
        assertFalse(featureOfAPoint.isEmpty());
        assertEquals(featureOfAPoint.getNumPoints(), 1);
        assertThat(featureOfAPoint.getCRS(), equalTo(CRS_EPSG_27572));

    }

    @Test
    public void script14()
    {

        TFeature featureOfAPolygon = TFeature(module).of(

                TPolygon(module)

                        .shell
                                (new double[][]
                                                {
                                                        {0, 0},
                                                        {1, 0},
                                                        {1, 1},
                                                        {0, 1},
                                                        {0, 0}
                                                }


                                ).geometry()
        ).geometry();

        assertFalse(featureOfAPolygon.isEmpty());
        assertEquals(featureOfAPolygon.getNumPoints(), 5);
        assertThat(featureOfAPolygon.getCRS(), equalTo(CRS_EPSG_27572));
    }

    @Test
    public void script15()
    {
        TFeature featureOfAPolygon = TFeature(module).of(

                TPolygon(module)

                        .shell
                                (new double[][]
                                                {
                                                        {0, 0},
                                                        {1, 0},
                                                        {1, 1},
                                                        {0, 1},
                                                        {0, 0}
                                                }
                                ).geometry()
        ).geometry();

        TFeatureCollection featureCollection = TFeatureCollection(module).of(featureOfAPolygon).geometry();
        assertFalse(featureCollection.isEmpty());
        assertEquals(featureCollection.getNumPoints(), 5);

        featureCollection.of(TFeature(module).of(TPoint(module).x(1d).y(2d).z(3d).geometry()).geometry());
        assertEquals(featureCollection.getNumPoints(), 6);
        assertThat(featureOfAPolygon.getCRS(), equalTo(CRS_EPSG_27572));
    }


}
