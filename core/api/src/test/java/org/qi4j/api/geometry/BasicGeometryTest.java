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
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        module.values( Coordinate.class, TLinearRing.class);

        // API values
        module.values(TPoint.class,TLineString.class, TPolygon.class);
    }

    @Test
    public void testWhenCreatedCoordinateNotNull()
    {
        ValueBuilder<Coordinate> builder = module.newValueBuilder(Coordinate.class);
        Coordinate coordinate = builder.prototype().of(1d,2d,3d);
        assertNotNull(coordinate);
    }

    @Test
    public void testWhenCreatedTGeomPointNotNull()
    {
        ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);

        assertNotNull(
            builder.prototype().of
            (
                    module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                    module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
            )
        );
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
                        module.newValueBuilder(TLinearRing.class).prototype().of
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
