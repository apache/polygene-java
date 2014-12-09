/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

import com.vividsolutions.jts.geom.MultiPolygon;
import org.junit.Test;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeomRoot;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGEOM.*;

/**
 * JAVADOC
 */
public class TGEOMTest
        extends AbstractQi4jTest {
    private final double ZERO = 0d;
    private final String CRS = "EPSG:27572";

    public void assemble(ModuleAssembly moduleAssembly)
            throws AssemblyException {
        // JJ TODO - What about to add this Geometry types to the "core" Qi4j, so that they
        // do not need to be added delicately ?

        // internal values
        moduleAssembly.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        moduleAssembly.values(TCRS.class, TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);

        TGeometry tGeometry = moduleAssembly.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS);

    }

    @Test
    public void testWhenCRSIsCreated() {
        String CRS = "urn:ogc:def:crs:OGC:1.3:CRS84";
        TCRS crs = TCRS(module).crs(CRS);
        assertThat(crs.crs(), equalTo( CRS ));
    }

    @Test
    public void testWhenPointIsCreated() {
        TPoint point_2D = TPOINT(module).x(1d).y(2d).geometry();
        assertEquals(1d, point_2D.x(), ZERO);
        assertEquals(2d, point_2D.y(), ZERO);
        assertTrue(point_2D.getCoordinates().length == 1);
        assertThat(point_2D.getCRS(), equalTo( CRS ));


        TPoint point_3D = TPOINT(module).x(1d).y(2d).z(3d).geometry();
        assertEquals(1d, point_3D.x(), ZERO);
        assertEquals(2d, point_3D.y(), ZERO);
        assertEquals(3d, point_3D.z(), ZERO);
        assertTrue(point_3D.getCoordinates().length == 1);
        assertThat(point_3D.getCRS(), equalTo( CRS ));

    }

    @Test
    public void testWhenMultiPointIsCreatedVerifyTypesAndValues() {

        TMultiPoint multiPoint = TMULTIPOINT(module).points(new double[][]
                {
                        {0d, 0d},
                        {1d, 1d},

                }).geometry();

        assertTrue(multiPoint.getNumGeometries() == 2);
        assertEquals(multiPoint.getType(), TMultiPoint.TGEOMETRY.MULTIPOINT);
        assertEquals(multiPoint.getGeometryN(0).getType(), TMultiPoint.TGEOMETRY.POINT);
        assertTrue(multiPoint.getCoordinates().length == 2);
        assertThat(multiPoint.getCRS(), equalTo( CRS ));



        assertEquals(0d, ((TPoint) multiPoint.getGeometryN(0)).x(), ZERO);
        assertEquals(0d, ((TPoint) multiPoint.getGeometryN(0)).y(), ZERO);
        assertThat(( multiPoint.getGeometryN(0)).getCRS(), equalTo( CRS ));


        assertEquals(1d, ((TPoint) multiPoint.getGeometryN(1)).x(), ZERO);
        assertEquals(1d, ((TPoint) multiPoint.getGeometryN(1)).y(), ZERO);

        // test DSL
        multiPoint.xy(2d, 2d);

        assertTrue(multiPoint.getNumGeometries() == 3);
        assertEquals(2d, ((TPoint) multiPoint.getGeometryN(2)).x(), ZERO);
        assertEquals(2d, ((TPoint) multiPoint.getGeometryN(2)).y(), ZERO);

        // overwrite & compare
        assertEquals(3d, ((TPoint) multiPoint.getGeometryN(2)).x(3).x(), ZERO);
        assertEquals(3d, ((TPoint) multiPoint.getGeometryN(2)).y(3).y(), ZERO);

    }


    @Test
    public void testWhenLineStringCreated() {

        TLineString line = TLINESTRING(module).points(new double[][]
                {
                        {0, 0},
                        {1, 0},
                        {1, 1},
                        {0, 1},
                        {0, 1}

                }).geometry();

        assertThat(line.getCRS(), equalTo( CRS ));
        assertFalse(line.isEmpty());
        assertFalse(line.isRing()); // is not a ring
        assertFalse(line.isClosed());
        assertTrue(line.getCoordinates().length == 5);


        System.out.println(line);
    }

    @Test
    public void testWhenLinearRingCreated() {

        TLinearRing ring = TLINEARRING(module).ring(new double[][]
                {
                        {0, 0},
                        {1, 0},
                        {1, 1},
                        {0, 1},
                        {0, 0}

                }).geometry();

        assertThat(ring.getCRS(), equalTo( CRS ));
        assertFalse(ring.isEmpty());
        assertTrue(ring.isRing());
        assertTrue(ring.isClosed());
        assertTrue(ring.getCoordinates().length == 5);

    }

    @Test
    public void testWhenLinearRingIsCleatedDSL()
    {
        TLinearRing tLinearRing = TLINEARRING(module).of().geometry();
    }

    @Test
    public void testWhenPolygonIsCreated() {
        TPolygon polygon = TPOLYGON(module)

                .shell
                        (
                                TLINEARRING(module).ring(new double[][]
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
                                TLINEARRING(module).ring(new double[][]
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
        System.out.println(polygon.getCoordinates()[1].x());
        System.out.println(polygon.getCoordinates()[1].y());


    }

    @Test
    public void testWhenPolygonIsCreatedWithoutHoles() {
        TPolygon polygon = TPOLYGON(module)

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
        assertThat(polygon.getCRS(), equalTo( CRS ));
        assertThat(polygon.shell().get().getCRS(), equalTo( CRS ));
        assertThat(polygon.shell().get().getPointN(0).getCRS(), equalTo( CRS ));


        // invalidate ring
        assertTrue(polygon.shell().get().isValid());
        assertEquals(1d, polygon.shell().get().getPointN(0).y(1).y(), ZERO);
        assertFalse(polygon.shell().get().isValid());


        System.out.println(polygon.getCoordinates()[1].x());
        System.out.println(polygon.getCoordinates()[1].y());

    }

    @Test
    public void testWhenPolygonIsCreatedWitHoles() {
        TPolygon polygon = TPOLYGON(module)

                .shell
                        (
                                new double[][]
                                        {
                                                {0, 0},
                                                {10, 0},
                                                {1, 10},
                                                {0, 10},
                                                {0, 0}
                                        }


                        )
                .withHoles(

                        TLINEARRING(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 0}

                                }).geometry()

                        , TLINEARRING(module).ring(new double[][]
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
        System.out.println(polygon.getCoordinates()[1].x());
        System.out.println(polygon.getCoordinates()[1].y());

        assertThat(polygon.getCRS(), equalTo( CRS ));
    }


    @Test
    public void testWhenMultiPolygonIsCreated() {

        TPolygon polygon1 = TPOLYGON(module)

                .shell
                        (
                                new double[][]
                                        {
                                                {0, 0},
                                                {10, 0},
                                                {0, 10},
                                                {0, 10},
                                                {0, 0}
                                        }


                        )
                .withHoles(

                        TLINEARRING(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 0}

                                }).geometry()

                        , TLINEARRING(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {2, 0},
                                        {2, 2},
                                        {0, 2},
                                        {0, 0}

                                }).geometry()


                )
                .geometry();

        TPolygon polygon2 = TPOLYGON(module)

                .shell
                        (
                                new double[][]
                                        {
                                                {0, 0},
                                                {20, 0},
                                                {1, 20},
                                                {0, 20},
                                                {0, 0}
                                        }


                        )
                .withHoles(

                        TLINEARRING(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {1, 0},
                                        {1, 1},
                                        {0, 1},
                                        {0, 0}

                                }).geometry()

                        , TLINEARRING(module).ring(new double[][]
                                {
                                        {0, 0},
                                        {2, 0},
                                        {2, 2},
                                        {0, 2},
                                        {0, 0}

                                }).geometry()


                )
                .geometry();

        TMultiPolygon multiPolygon = TMULTIPOLYGON(module).of(polygon1, polygon2).geometry();
        assertTrue(multiPolygon.getNumGeometries() == 2);
        assertTrue(multiPolygon.getCoordinates().length == 30);
        System.out.println(multiPolygon.getGeometryN(0).getNumPoints());

        assertThat(multiPolygon.getCRS(), equalTo( CRS ));


    }

    @Test
    public void testWhenFeatureCreated() {

        TFeature featureOfAPoint = TFEATURE(module).of(TPOINT(module).x(1d).y(2d).z(3d).geometry()).geometry();
        assertEquals(featureOfAPoint.getType(), TMultiPoint.TGEOMETRY.FEATURE);
        assertFalse(featureOfAPoint.isEmpty() );
        assertEquals(featureOfAPoint.getNumPoints(), 1);
        assertThat(featureOfAPoint.getCRS(), equalTo( CRS ));

    }

    @Test
    public void testWhenFeatureOfPolygonIsCreated() {

      TFeature featureOfAPolygon =  TFEATURE(module).of(

                TPOLYGON(module)

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

        assertFalse(featureOfAPolygon.isEmpty() );
        assertEquals(featureOfAPolygon.getNumPoints(), 5);
        assertThat(featureOfAPolygon.getCRS(), equalTo( CRS ));

    }

    @Test
    public void testWhenFeatureCollectionIsCreated() {
        TFeature featureOfAPolygon =  TFEATURE(module).of(

                TPOLYGON(module)

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

        TFeatureCollection featureCollection = TFEATURECOLLECTION(module).of(featureOfAPolygon).geometry();
        assertFalse(featureCollection.isEmpty() );
        assertEquals(featureCollection.getNumPoints(), 5);

        featureCollection.of(TFEATURE(module).of(TPOINT(module).x(1d).y(2d).z(3d).geometry()).geometry());
        assertEquals(featureCollection.getNumPoints(), 6);
        assertThat(featureOfAPolygon.getCRS(), equalTo( CRS ));

    }


}
