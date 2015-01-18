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

package org.qi4j.index.elasticsearch.extensions.spatial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.ElasticSearchIndexException;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper;
import org.qi4j.library.spatial.projections.ProjectionsRegistry;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper.IndexMappingCache;
import static org.qi4j.library.spatial.projections.transformations.TTransformations.Transform;

public final class ElasticSearchSpatialIndexer
{

    private static final String EPSG_4326 = "EPSG:4326";
    private static final String DefaultSupportedProjection = EPSG_4326;
    private static final double DefaultProjectionConversionPrecisionInMeters = 2.00;
    private Module module;

    private ElasticSearchSpatialIndexer()
    {
    }

    public static void toJSON(ElasticSearchSupport support, TGeometry geometry, String property, String deepProperty, JSONObject json, Module module) throws ElasticSearchIndexException
    {

        // Spatial Mappings
        {
            if (SpatialConfiguration.isEnabled(support.spatialConfiguration()))
            {
                SpatialIndexMapper.createIfNotExist(support, geometry, deepProperty);
            } else throw new ElasticSearchIndexException("Spatial support is disabled. No spatial indexing available");
        }

        // Spatial Transformations
        {
            if (new ProjectionsRegistry().getCRS(geometry.getCRS()) == null)
                throw new ElasticSearchIndexException("Project with the CRS Identity " + geometry.getCRS() + " is unknown. Supported projections are JJ TODO");
            if (SpatialConfiguration.isIndexerProjectionConversionEnabled(support.spatialConfiguration()))
            {
                Transform(module).from(geometry).to(DefaultSupportedProjection, SpatialConfiguration.getIndexerProjectionConversionAccuracy(support.spatialConfiguration()));
            } else if (!geometry.getCRS().equalsIgnoreCase(DefaultSupportedProjection))
                throw new ElasticSearchIndexException("Project with the CRS Identity " + geometry.getCRS() + " is not supported by ElasticSearch and projection conversion is " +
                        "disabled in the configuration.");
        }

        try
        {

            if (geometry.isPoint())
            {
                if (IndexMappingCache.isMappedAsGeoPoint(support.index(), support.entitiesType(), deepProperty))
                {
                    createIndexPointAsGeoPointType(property, json, (TPoint) geometry);
                } else if (IndexMappingCache.isMappedAsGeoShape(support.index(), support.entitiesType(), deepProperty))
                {
                    createIndexPointAsGeoShapeType(property, json, (TPoint) geometry);
                } else
                    new ElasticSearchIndexException("No spatial mapping for property " + deepProperty + " available.");
            } else if (geometry.isMultiPoint())
            {
                createIndexMultiPoint(property, json, (TMultiPoint) geometry);
            } else if (geometry.isLineString())
            {
                createIndexLineString(property, json, (TLineString) geometry);
            } else if (geometry.isMultiLineString())
            {
                createIndexMultiLineString(property, json, (TMultiLineString) geometry);
            } else if (geometry.isPolygon())
            {
                createIndexPolygon(property, json, (TPolygon) geometry);
            } else if (geometry.isMultiPolygon())
            {
                createIndexMultiPolygon(property, json, (TMultiPolygon) geometry);
            } else if (geometry.isFeature())
            {
                createIndexFeature(property, json, (TFeature) geometry);
            } else if (geometry.isFeatureCollection())
            {
                createIndexFeatureCollection(property, json, (TFeatureCollection) geometry);
            } else new ElasticSearchIndexException("Unsupported Geometry : " + geometry.getClass());

        } catch (JSONException _ex)
        {
            throw new ElasticSearchIndexException("", _ex);
        }
    }

    private static void createIndexMultiPoint(String property, JSONObject json, TMultiPoint tMultiPoint) throws JSONException
    {

        Map tMultiPointMap = new HashMap();
        tMultiPointMap.put("type", "multipoint");

        JSONArray points = new JSONArray();
        for (int i = 0; i < tMultiPoint.getNumPoints(); i++)
        {
            TPoint point = (TPoint) tMultiPoint.getGeometryN(i);
            points.put(new JSONArray().put(point.y()).put(point.x()));
        }

        tMultiPointMap.put("coordinates", points);
        json.put(property, tMultiPointMap);

    }

    private static void createIndexLineString(String property, JSONObject json, TLineString tLineString) throws JSONException
    {
        Map tLineStringMap = new HashMap();
        tLineStringMap.put("type", "linestring");

        JSONArray points = new JSONArray();
        for (int i = 0; i < tLineString.getNumPoints(); i++)
        {
            TPoint point = (TPoint) tLineString.getPointN(i);
            points.put(new JSONArray().put(point.y()).put(point.x()));
        }

        tLineStringMap.put("coordinates", points);
        json.put(property, tLineStringMap);
    }

    /**
     * !! ATTENTION !!
     * <p/>
     * The ES documentation at http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/mapping-geo-point-type.html
     * is WRONG as it defines the indexing structure for a "geo_point" as
     * <p/>
     * {
     * "pin" : {
     * "location" : {
     * "lat" : 41.12,
     * "lon" : -71.34
     * }
     * }
     * }
     * <p/>
     * But is has to be
     * <p/>
     * {
     * "pin" :{
     * "lat":41.12,,
     * "lon":-71.34
     * }
     * }
     * }
     * <p/>
     * Otherwise the GeoUtil.java ES Parser is not able to parse/find the lat/log fields and throws an Exception on the ES-Node (Serverside).
     *
     * @param property
     * @param json
     * @param tPoint
     * @throws Exception
     */
    private static void createIndexPointAsGeoPointType(String property, JSONObject json, TPoint tPoint) throws JSONException
    {
        Map tPointMap = new HashMap();
        tPointMap.put("lat", tPoint.y());
        tPointMap.put("lon", tPoint.x());

        json.put(property, tPointMap);
    }

    private static void createIndexPointAsGeoShapeType(String property, JSONObject json, TPoint tPoint) throws JSONException
    {
        Map tPointMap = new HashMap();
        tPointMap.put("type", "point");
        tPointMap.put("coordinates", new JSONArray().put(tPoint.x()).put(tPoint.y()));

        json.put(property, tPointMap);
    }

    private static void createIndexMultiLineString(String property, JSONObject json, TMultiLineString tMultiLineString) throws JSONException
    {
        Map tMultiLineStringMap = new HashMap();
        tMultiLineStringMap.put("type", "multilinestring");

        JSONArray coordinates = new JSONArray();

        for (int i = 0; i < tMultiLineString.geometries().get().size(); i++)
        {
            JSONArray p = new JSONArray();
            int nPoints = ((TLineString) tMultiLineString.getGeometryN(i)).getNumPoints();

            JSONArray line = new JSONArray();
            for (int j = 0; j < nPoints; j++)
            {
                JSONArray xy = new JSONArray();
                xy.put(((TLineString) tMultiLineString.getGeometryN(i)).getPointN(j).x());
                xy.put(((TLineString) tMultiLineString.getGeometryN(i)).getPointN(j).y());
                line.put(xy);
            }
            coordinates.put(line);
        }
        tMultiLineStringMap.put("coordinates", coordinates);
        json.put(property, tMultiLineStringMap);
    }

    private static void createIndexPolygon(String property, JSONObject json, TPolygon tPolygon) throws JSONException
    {
        if (!tPolygon.shell().get().isValid())
            throw new ElasticSearchIndexException("Polygon shell has to be closed - first and last point must match. ");

        Map tPolygonMap = new HashMap();
        tPolygonMap.put("type", "polygon");

        JSONArray coordinates = new JSONArray();

        // shell
        {
            JSONArray shell = new JSONArray();
            for (int i = 0; i < tPolygon.shell().get().getNumPoints(); i++)
            {
                JSONArray p = new JSONArray();

                p.put(tPolygon.shell().get().getPointN(i).x());
                p.put(tPolygon.shell().get().getPointN(i).y());

                shell.put(p);
            }
            coordinates.put(shell);
        }

        // wholes
        {
            for (int i = 0; i < tPolygon.holes().get().size(); i++)
            {
                JSONArray whole = new JSONArray();
                // TLinearRing whole = tPolygon.holes().get().get(i);
                for (int j = 0; j < tPolygon.holes().get().get(i).getNumPoints(); j++)
                {
                    if (!tPolygon.holes().get().get(i).isValid())
                        throw new ElasticSearchIndexException("Polygon whole has to be closed - first and last point must match. ");

                    JSONArray p = new JSONArray();

                    p.put(tPolygon.holes().get().get(i).getPointN(j).x());
                    p.put(tPolygon.holes().get().get(i).getPointN(j).y());

                    whole.put(p);
                }
                coordinates.put(whole);
            }
        }

        tPolygonMap.put("coordinates", coordinates);
        json.put(property, tPolygonMap);
    }

    private static void createIndexMultiPolygon(String property, JSONObject json, TMultiPolygon tMultiPolygon) throws JSONException
    {
        Map tMultiPolygonMap = new HashMap();
        tMultiPolygonMap.put("type", "multipolygon");
        JSONArray coordinates = new JSONArray();

        for (int i = 0; i < tMultiPolygon.geometries().get().size(); i++)
        {
            JSONObject _json = new JSONObject();
            createIndexPolygon(property, _json, (TPolygon) tMultiPolygon.getGeometryN(i));
            coordinates.put(((JSONObject) _json.get(property)).get("coordinates"));
        }
        tMultiPolygonMap.put("coordinates", coordinates);
        json.put(property, tMultiPolygonMap);
    }

    private static void createIndexFeatureCollection(String property, JSONObject json, TFeatureCollection tFeatureCollection) throws JSONException
    {
        Map tFeatureMap = new HashMap();
        tFeatureMap.put("type", "geometrycollection");

        JSONArray geometries = new JSONArray();
        JSONObject _json = new JSONObject();

        for (TGeometry tGeometry : tFeatureCollection.geometries().get())
        {
            TFeature tFeature = (TFeature) tGeometry;
            switch (tFeature.asGeometry().getType())
            {
                case POINT:
                    createIndexPointAsGeoShapeType(property, _json, (TPoint) tFeature.asGeometry());
                    break;
                case MULTIPOINT:
                    createIndexMultiPoint(property, _json, (TMultiPoint) tFeature.asGeometry());
                    break;
                case LINESTRING:
                    createIndexLineString(property, _json, (TLineString) tFeature.asGeometry());
                    break;
                case MULTILINESTRING:
                    createIndexMultiLineString(property, _json, (TMultiLineString) tFeature.asGeometry());
                    break;
                case POLYGON:
                    createIndexPolygon(property, _json, (TPolygon) tFeature.asGeometry());
                    break;
                case MULTIPOLYGON:
                    createIndexMultiPolygon(property, _json, (TMultiPolygon) tFeature.asGeometry());
                    break;
            }
            geometries.put(_json.get(property));
        }

        tFeatureMap.put("geometries", geometries);
        json.put(property, tFeatureMap);
    }

    private static void createIndexFeature(String property, JSONObject json, TFeature tFeature) throws JSONException
    {
        Map tFeatureMap = new HashMap();
        tFeatureMap.put("type", "geometrycollection");

        JSONArray geometries = new JSONArray();
        JSONObject _json = new JSONObject();

        switch (tFeature.asGeometry().getType())
        {
            case POINT:
                createIndexPointAsGeoShapeType(property, _json, (TPoint) tFeature.asGeometry());
                break;
            case MULTIPOINT:
                createIndexMultiPoint(property, _json, (TMultiPoint) tFeature.asGeometry());
                break;
            case LINESTRING:
                createIndexLineString(property, _json, (TLineString) tFeature.asGeometry());
                break;
            case MULTILINESTRING:
                createIndexMultiLineString(property, _json, (TMultiLineString) tFeature.asGeometry());
                break;
            case POLYGON:
                createIndexPolygon(property, _json, (TPolygon) tFeature.asGeometry());
                break;
            case MULTIPOLYGON:
                createIndexMultiPolygon(property, _json, (TMultiPolygon) tFeature.asGeometry());
                break;
        }

        geometries.put(_json.get(property));

        tFeatureMap.put("geometries", geometries);
        json.put(property, tFeatureMap);
    }

    public static String spatialMappingPropertyName(Stack<String> stack)
    {
        ListIterator<String> it = stack.listIterator();
        if (!it.hasNext()) return "";
        StringBuilder sb = new StringBuilder();
        for (; ; )
        {
            String s = it.next();
            sb.append(s);
            if (!it.hasNext())
                return sb.toString();
            sb.append('.');
        }
    }

}
