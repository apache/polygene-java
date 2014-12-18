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

package org.qi4j.api.geometry.internal;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

@Mixins(TGeometry.Mixin.class)
public interface TGeometry extends TGeometryRoot {

    // @Optional
    Property<TGEOMETRY_TYPE> geometryType();

    // Property<TGEOM_TYPE> type1();

    @Optional
    @UseDefaults
    Property<String> CRS();


    String getCRS();

    void setCRS(String crs);


    abstract Coordinate[] getCoordinates();

    abstract int getNumPoints();

    abstract boolean isEmpty();

    TGEOMETRY_TYPE getType();


    public abstract class Mixin implements TGeometry {

        @Structure
        Module module;

        @This
        TGeometry self;

        public String getCRS() {
            return self.CRS().get();
        }

        public void setCRS(String crs) {
            self.CRS().set(crs);
        }


        public int getNumPoints() {
            return 0;
        }

        public Coordinate[] getCoordinates() {
            return null;
        }

        public boolean isEmpty() {
            throw new RuntimeException("Should never be called");
        }

        public TGEOMETRY_TYPE getType() {
            return self.geometryType().get();
        }

    }

}
