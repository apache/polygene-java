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
import org.qi4j.api.geometry.internal.builders.TPointBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;
import static org.qi4j.api.geometry.TGEOM.*;

/**
 * JAVADOC
 */
public class TGEOMTest
        extends AbstractQi4jTest {
    private final double ZERO = 0d;

    public void assemble(ModuleAssembly module)
            throws AssemblyException {
        // JJ TODO - What about to add this Geometry types to the "core" Qi4j, so that they
        // do not need to be added delicately ?

        // internal values
        module.values(Coordinate.class, TLinearRing.class);

        // API values
        module.values(TPoint.class, TLineString.class, TPolygon.class);
    }

    @Test
    public void testWhenPointIsCreated() {
        TPoint point = TPOINT(module).x(1d).y(2d).z(3d).geometry();
        assertEquals(1d, point.x(), ZERO);
        assertEquals(2d, point.y(), ZERO);
        assertEquals(3d, point.z(), ZERO);
    }

    @Test
    public void testWhenLinearRingCreated() {
        // TLINEARRING(module).xyz(new double[]{0d, 1d});
        TLinearRing ring = TLINEARRING(module).ring(new double[][]
                {
                        {0, 0},
                        {1, 0},
                        {1, 1},
                        {0, 1},
                        {0, 0}

                }).geometry();

        System.out.println(ring);
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

        assertTrue(polygon.getNumPoints()           == 10);
        assertTrue(polygon.getCoordinates().length  == 10);
        System.out.println(polygon.getCoordinates()[1].x());
        System.out.println(polygon.getCoordinates()[1].y());



    }

    @Test
    public void testWhenPolygonIsCreatedV2() {
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


        assertTrue(polygon.getNumPoints()           == 5);
        assertTrue(polygon.getCoordinates().length  == 5);
        System.out.println(polygon.getCoordinates()[1].x());
        System.out.println(polygon.getCoordinates()[1].y());



    }


}
