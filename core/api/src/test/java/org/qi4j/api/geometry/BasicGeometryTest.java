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

import org.junit.Test;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class BasicGeometryTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // JJ TODO - What about to add this Geometry types to the "core" Qi4j, so that they
        // do not need to be added delicately ?

        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TCRS.class, TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);
    }

    @Test
    public void testWhenCRSIsCreated()
    {
        String CRS = "urn:ogc:def:crs:OGC:1.3:CRS84";
        ValueBuilder<TCRS> builder = module.newValueBuilder(TCRS.class);
        TCRS crs = builder.prototype().of(CRS);
        assertThat(crs.crs(), equalTo( CRS ));
    }

        @Test
    public void testWhenCoordinatedCreated()
    {
        ValueBuilder<Coordinate> builder = module.newValueBuilder(Coordinate.class);
        Coordinate coordinate1 = builder.prototype().of(1d,2d,3d);

        assertNotNull(coordinate1);
        assertEquals(1d, coordinate1.x(), 0.0d);
        assertEquals(2d, coordinate1.y(), 0.0d);
        assertEquals(3d, coordinate1.z(), 0.0d);
        assertTrue(coordinate1.compareTo(module.newValueBuilder(Coordinate.class).prototype().x(1d).y(2d).z(3d)) == 0);
        assertTrue(coordinate1.compareTo(module.newValueBuilder(Coordinate.class).prototype().x(1d).y(1d).z(1d)) != 0);

    }

    @Test
    public void testWhenPointCreated()
    {
        ValueBuilder<TPoint> builder1 = module.newValueBuilder(TPoint.class);

        TPoint point1 = builder1.prototype().x(1d).y(2d).z(3d);
        assertEquals(1d, point1.x(), 0.0d);
        assertEquals(2d, point1.y(), 0.0d);
        assertEquals(3d, point1.z(), 0.0d);

       // assertTrue(point1.isEmpty() == false);

        ValueBuilder<TPoint> builder2 = module.newValueBuilder(TPoint.class);

        TPoint point2 = builder2.prototype().of(1d,2d,3d);
        assertEquals(1d, point2.x(), 0.0d);
        assertEquals(2d, point2.y(), 0.0d);
        assertEquals(3d, point2.z(), 0.0d);


        ValueBuilder<TPoint> builder3 = module.newValueBuilder(TPoint.class);

        TPoint point3 = builder3.prototype().x(1d).of().y(2d).of().z(3d).of(); // check dsl
        assertEquals(1d, point3.x(), 0.0d);
        assertEquals(2d, point3.y(), 0.0d);
        assertEquals(3d, point3.z(), 0.0d);

        ValueBuilder<TPoint> builder4 = module.newValueBuilder(TPoint.class);

        TPoint point4 = builder4.prototype().x(10d).y(20d).z(30d).of(1d,2d,3d); // check dsl
        assertEquals(1d, point4.x(), 0.0d);
        assertEquals(2d, point4.y(), 0.0d);
        assertEquals(3d, point4.z(), 0.0d);

    }


    @Test
    public void testWhenLineStringCreated()
    {
        ValueBuilder<TLineString> builder = module.newValueBuilder(TLineString.class);

        TLineString lineString1 = builder.prototype().of(
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(0d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(1d).z(0),
                module.newValueBuilder(TPoint.class).prototype().x(2d).y(2d).z(0)
        );

        assertTrue(lineString1.isEmpty() == false);
        assertTrue(lineString1.getNumPoints() == 3);

        assertEquals(0d, lineString1.getPointN(0).x(), 0.0d);
        assertEquals(0d, lineString1.getPointN(0).y(), 0.0d);
        assertEquals(0d, lineString1.getPointN(0).z(), 0.0d);

        assertEquals(0d, lineString1.getPointN(1).x(), 0.0d);
        assertEquals(1d, lineString1.getPointN(1).y(), 0.0d);
        assertEquals(0d, lineString1.getPointN(1).z(), 0.0d);

        assertEquals(0d, lineString1.getStartPoint().x(), 0.0d);
        assertEquals(2d, lineString1.getEndPoint().x(), 0.0d);
    }

    @Test
    public void testWhenLineStringCreatedV2() {
        ValueBuilder<TLineString> builder = module.newValueBuilder(TLineString.class);

        TLineString lineString = builder.prototype().of()

                .xy(0d, 0d)
                .xy(0d, 1d)
                .xy(1d, 0d)
                .xy(1d, 1d)
                .xy(0d, 0d);

        assertTrue(lineString.getStartPoint().x() == 0d);

    }


        @Test
    public void testWhenValidLinearRingIsCreated()
    {
        ValueBuilder<TLinearRing> builder = module.newValueBuilder(TLinearRing.class);

        TLinearRing linearRing = (TLinearRing)builder.prototype().of(
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(0d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(1d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(1d).y(0d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(1d).y(1d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(0d).z(-1d)
        );

        assertTrue(linearRing.isValid()); // ring closed ?, z-dimension is ignored
    }

    @Test
    public void testWhenInvalidLinearRingIsCreated()
    {
        ValueBuilder<TLinearRing> builder = module.newValueBuilder(TLinearRing.class);

        TLinearRing linearRing = (TLinearRing)builder.prototype().of(
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(0d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(1d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(1d).y(0d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(1d).y(1d).z(0d),
                module.newValueBuilder(TPoint.class).prototype().x(0d).y(-1d).z(-1d)
        );

        assertFalse(linearRing.isValid()); // ring closed ?, z-dimension is ignored
    }

    @Test
    public void testWhenInvalidLinearRingIsCreatedV2()
    {
        ValueBuilder<TLinearRing> builder = module.newValueBuilder(TLinearRing.class);

        TLinearRing shell = (TLinearRing)builder.prototype().of()

                .xy(0d, 0d)
                .xy(0d, 1d)
                .xy(1d, 0d)
                .xy(1d, 1d)
                .xy(0d, 0d);


        assertTrue(shell.isValid()); // ring closed ?, z-point-dimension is ignored
        assertTrue(shell.getNumPoints() == 5);
    }


    @Test
    public void testWhenPolygonIsCreated()
    {
        ValueBuilder<TPolygon> builder = module.newValueBuilder(TPolygon.class);
/**
        builder.prototype().of(
                module.newValueBuilder((TLinearRing.class).prototype().of()
                        .xy(0d,  0d)
                        .xy(0d, 10d)
                        .xy(10d, 0d)
                        .xy(1d, 10d)
                        .xy(0d, 0d)
        );
*/
        //builder.prototype().of(
        //        module.newValueBuilder(TLinearRing.class).prototype().
    }


    @Test
    public void testWhenCreatedTGeomLineStringNotNull()
    {
        ValueBuilder<TLineString> builder = module.newValueBuilder(TLineString.class);
        assertNotNull(
        builder.prototype().of
                (
                    module.newValueBuilder(TPoint.class).prototype().of
                    (
                        module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                        module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
                    )
                 ,
                    module.newValueBuilder(TPoint.class).prototype().of
                    (
                        module.newValueBuilder(Coordinate.class).prototype().of(2d),  //x
                        module.newValueBuilder(Coordinate.class).prototype().of(2d)   //y
                    )

                )
        );

    }

    @Test
    public void testWhenCreatedLinearRingNotNull()
    {
        ValueBuilder<TLinearRing> builder = module.newValueBuilder(TLinearRing.class);
        assertNotNull(
        builder.prototype().of
                (
                        module.newValueBuilder(TPoint.class).prototype().of
                                (
                                        module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                                        module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
                                )
                        ,
                        module.newValueBuilder(TPoint.class).prototype().of
                                (
                                        module.newValueBuilder(Coordinate.class).prototype().of(2d),  //x
                                        module.newValueBuilder(Coordinate.class).prototype().of(2d)   //y
                                )

                )
        );

    }

    @Test
    public void testWhenCreatedTGeomPolygonWithNoHolesNotNull()
    {
        ValueBuilder<TPolygon> builder = module.newValueBuilder(TPolygon.class);
        assertNotNull(
        builder.prototype().of
                (
                        // shell
                        (TLinearRing)module.newValueBuilder(TLinearRing.class).prototype().of
                                (
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(2d)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(2d),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(2d)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(2d),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
                                                )

                                )
                      ,
                       // no holes
                       null
                )
        );

    }

    @Test
    public void testWhenCreatedTGeomPolygonWithWithHolesNotNull()
    {
        // JJ TODO - add test polygon and holes
    }



}
