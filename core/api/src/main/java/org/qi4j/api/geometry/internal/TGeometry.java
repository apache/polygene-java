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
import org.qi4j.api.geometry.*;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;

@Mixins(TGeometry.Mixin.class)
public interface TGeometry extends ValueComposite
{
    enum TGEOMETRY_TYPE
    {
        POINT, MULTIPOINT, LINESTRING, MULTILINESTRING, POLYGON, MULTIPOLYGON, FEATURE, FEATURECOLLECTION, INVALID
    }

    Property<TGEOMETRY_TYPE> geometryType();

    @Optional
    @UseDefaults
    Property<String> CRS();
    String getCRS();
    void setCRS(String crs);

    abstract Coordinate[] getCoordinates();

    abstract int getNumPoints();

    abstract boolean isEmpty();

    TGEOMETRY_TYPE getType();

    boolean isPoint();
    boolean isPoint(TGeometry tGeometry);

    boolean isMultiPoint();
    boolean isMultiPoint(TGeometry tGeometry);

    boolean isLineString();
    boolean isLineString(TGeometry tGeometry);

    boolean isMultiLineString();
    boolean isMultiLineString(TGeometry tGeometry);

    boolean isPolygon();
    boolean isPolygon(TGeometry tGeometry);

    boolean isMultiPolygon();
    boolean isMultiPolygon(TGeometry tGeometry);

    boolean isFeature();
    boolean isFeature(TGeometry tGeometry);

    boolean isFeatureCollection();
    boolean isFeatureCollection(TGeometry tGeometry);

    boolean isGeometry();
    boolean isGeometry(Object tGeometry);



    public abstract class Mixin implements TGeometry
    {

        @Structure
        Module module;

        @This
        TGeometry self;

        public String getCRS()
        {
            return self.CRS().get();
        }

        public void setCRS(String crs)
        {
            self.CRS().set(crs);
        }


        public int getNumPoints()
        {
            return 0;
        }

        public Coordinate[] getCoordinates()
        {
            throw new RuntimeException("Should never be called");
        }

        public boolean isEmpty()
        {
            throw new RuntimeException("Should never be called");
        }

        public TGEOMETRY_TYPE getType()
        {
            // "strong typing" - type & instanceOf must match
            switch (self.geometryType().get())
            {
                case POINT:
                    return self.isPoint() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.POINT;
                case MULTIPOINT:
                    return self.isMultiPoint() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.MULTIPOINT;
                case LINESTRING:
                    return self.isLineString() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.LINESTRING;
                case MULTILINESTRING:
                    return self.isMultiLineString() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.MULTILINESTRING;
                case POLYGON:
                    return self.isPolygon() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.POLYGON;
                case MULTIPOLYGON:
                    return self.isMultiPolygon() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.MULTIPOLYGON;
                case FEATURE:
                    return self.isFeature() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.FEATURE;
                case FEATURECOLLECTION:
                    return self.isFeatureCollection() == false ? TGEOMETRY_TYPE.INVALID : TGEOMETRY_TYPE.FEATURECOLLECTION;
                default:
                    return TGEOMETRY_TYPE.INVALID;
            }
        }

        public boolean isPoint()
        {
            return self instanceof TPoint ? true : false;
        }
        public boolean isPoint(TGeometry tGeometry)
        {
            return tGeometry instanceof TPoint ? true : false;
        }

        public boolean isMultiPoint()
        {
            return self instanceof TMultiPoint ? true : false;
        }
        public boolean isMultiPoint(TGeometry tGeometry)
        {
            return tGeometry instanceof TMultiPoint ? true : false;
        }

        public boolean isLineString()
        {
            return self instanceof TLineString ? true : false;
        }
        public boolean isLineString(TGeometry tGeometry)
        {
            return tGeometry instanceof TLineString ? true : false;
        }

        public boolean isMultiLineString()
        {
            return self instanceof TMultiLineString ? true : false;
        }
        public boolean isMultiLineString(TGeometry tGeometry)
        {
            return tGeometry instanceof TMultiLineString ? true : false;
        }

        public boolean isPolygon()
        {
            return self instanceof TPolygon ? true : false;
        }
        public boolean isPolygon(TGeometry tGeometry)
        {
            return tGeometry instanceof TPolygon ? true : false;
        }

        public boolean isMultiPolygon()
        {
            return self instanceof TMultiPolygon ? true : false;
        }
        public boolean isMultiPolygon(TGeometry tGeometry)
        {
            return tGeometry instanceof TMultiPolygon ? true : false;
        }

        public boolean isFeature()
        {
            return self instanceof TFeature ? true : false;
        }
        public boolean isFeature(TGeometry tGeometry)
        {
            return tGeometry instanceof TFeature ? true : false;
        }

        public boolean isFeatureCollection()
        {
            return self instanceof TFeatureCollection ? true : false;
        }
        public boolean isFeatureCollection(TGeometry tGeometry)
        {
            return tGeometry instanceof TFeatureCollection ? true : false;
        }

        public boolean isGeometry()
        {
            return self instanceof TGeometry ? true : false;
        }
        public boolean isGeometry(Object tGeometry)
        {
            return tGeometry instanceof TGeometry ? true : false;
        }
    }

}
