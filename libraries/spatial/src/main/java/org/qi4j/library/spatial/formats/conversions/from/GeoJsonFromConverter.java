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

package org.qi4j.library.spatial.formats.conversions.from;

import org.geojson.*;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;

import java.util.List;

import static org.qi4j.api.geometry.TGeometryFactory.*;


public class GeoJsonFromConverter
{

    private Module module;

    public GeoJsonFromConverter(Module module)
    {
        this.module = module;
    }

    public TGeometry convert(GeoJsonObject geojson)

    {
        return transform(geojson);
    }

    private TGeometry transform(GeoJsonObject geojson)
    {
        if (geojson instanceof Point)
        {
            return createTPoint((Point) geojson);
        } else if ((geojson instanceof MultiPoint) && !(geojson instanceof LineString))
        {
            return createTMultiPoint((MultiPoint) geojson);
        } else if (geojson instanceof LineString)
        {
            return createTLineString((LineString) geojson);
        } else if (geojson instanceof MultiLineString)
        {
            return createTMultiLineString((MultiLineString) geojson);
        } else if (geojson instanceof Polygon)
        {
            return createTPolygon((Polygon) geojson);
        } else if (geojson instanceof MultiPolygon)
        {
            return createTMultiPolygon((MultiPolygon) geojson);
        } else if (geojson instanceof Feature)
        {
            return createTFeature((Feature) geojson);
        } else if (geojson instanceof FeatureCollection)
        {
            return createTFeatureCollection((FeatureCollection) geojson);
        } else throw new RuntimeException("Unknown GeoJSON type - " + geojson);
    }


    private TGeometry createTPoint(Point point)
    {
        return TPoint(module)
                .x(point.getCoordinates().getLatitude())
                .y(point.getCoordinates().getLongitude())
                .z(point.getCoordinates().getAltitude())
                .geometry();
    }

    private TGeometry createTMultiPoint(MultiPoint multiPoint)
    {
        TMultiPoint tMultiPoint = TMultiPoint(module).geometry();
        for (LngLatAlt xyz : multiPoint.getCoordinates())
        {
            tMultiPoint.of
                    (
                            TPoint(module)
                                    .x(xyz.getLatitude())
                                    .y(xyz.getLongitude())
                                    .z(xyz.getAltitude())
                                    .geometry()
                    );
        }
        return tMultiPoint;
    }

    private TGeometry createTLineString(LineString lineString)
    {
        TLineString tLineString = TLineString(module).of().geometry();

        for (LngLatAlt xyz : lineString.getCoordinates())
        {
            tLineString.of(
                    TPoint(module)
                            .x(xyz.getLatitude())
                            .y(xyz.getLongitude())
                            .z(xyz.getAltitude())
                            .geometry()
            );
        }
        return tLineString;
    }

    private TGeometry createTMultiLineString(MultiLineString multiLineString)
    {
        TMultiLineString tMultiLineString = TMultiLineString(module).of().geometry();
        for (List<LngLatAlt> coordinates : multiLineString.getCoordinates())
        {
            tMultiLineString.of(getLine(coordinates));
        }
        return tMultiLineString;
    }

    private TGeometry createTPolygon(Polygon polygon)
    {
        TPolygon tPolygon;
        TLinearRing ring = getRing((polygon).getExteriorRing());
        if (!ring.isValid())
            throw new RuntimeException("Polygon shell not valid");
        else
            tPolygon = TPolygon(module).shell(ring).geometry();
        for (int i = 0; i < (polygon).getInteriorRings().size(); i++)
        {
            tPolygon.withHoles(getRing((polygon).getInteriorRings().get(i)));
        }
        return tPolygon;
    }

    private TGeometry createTMultiPolygon(MultiPolygon multiPolygon)
    {
        TMultiPolygon tMultiPolygon = TMultiPolygon(module).of().geometry();
        for (List<List<LngLatAlt>> polygons : multiPolygon.getCoordinates())
        {
            for (List<LngLatAlt> polygon : polygons)
            {
                tMultiPolygon.of(TPolygon(module).shell(getRing(polygon)).geometry());
            }
        }
        return tMultiPolygon;
    }

    private TGeometry createTFeature(Feature feature)
    {
        return TFeature(module).of(new GeoJsonFromConverter(module).transform(feature.getGeometry())).geometry();
    }

    private TGeometry createTFeatureCollection(FeatureCollection featurecollection)
    {
        TFeatureCollection tFeatureCollection = TFeatureCollection(module).of().geometry();
        for (Feature feature : featurecollection.getFeatures())
        {
            tFeatureCollection.of((TFeature) createTFeature(feature));
        }
        return tFeatureCollection;
    }

    private TLineString getLine(List<LngLatAlt> coordinates)
    {
        TLineString tLineString = TLineString(module).of().geometry();
        for (LngLatAlt xyz : coordinates)
        {
            tLineString.yx(xyz.getLatitude(), xyz.getLongitude());
        }
        return tLineString;
    }

    private TLinearRing getRing(List<LngLatAlt> coordinates)
    {

        TLinearRing tLinearRing = TLinearRing(module).of().geometry();
        for (LngLatAlt xyz : coordinates)
        {
            tLinearRing.yx(xyz.getLatitude(), xyz.getLongitude());
        }

        if (!tLinearRing.isClosed())
        {
            tLinearRing.of(tLinearRing.getStartPoint()); // hack here - we are closing the ring, of not closed.
        }

        return tLinearRing;
    }
}
