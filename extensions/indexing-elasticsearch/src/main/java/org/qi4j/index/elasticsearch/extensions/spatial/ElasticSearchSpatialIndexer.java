package org.qi4j.index.elasticsearch.extensions.spatial;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.ElasticSearchIndexException;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper;
import org.qi4j.library.spatial.v2.projections.ProjectionsRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper.IndexMappingCache;
import static org.qi4j.library.spatial.v2.transformations.TTransformations.Transform;

public final class ElasticSearchSpatialIndexer {

    private static final String EPSG_4326 = "EPSG:4326";
    private static final String DefaultProjection = EPSG_4326;
    private static final double DefaultProjectionConversionPrecisionInMeters = 2.00;

    public static void toJSON(ElasticSearchSupport support, TGeometry geometry, String property, String propertyWithDepth, JSONObject json, Module module) throws ElasticSearchIndexException {

        // Spatial Mappings
        {
            SpatialIndexMapper.createIfNotExist(support, geometry, propertyWithDepth);
        }

        // Spatial Transformations
        {
            if (new ProjectionsRegistry().getCRS(geometry.getCRS()) == null)
                throw new ElasticSearchIndexException("Project with the CRS Identity " + geometry.getCRS() + " is unknown. Supported projections are JJ TODO");
            // Transform(module).from(geometry).to(DefaultProjection, DefaultProjectionConversionPrecisionInMeters);
        }


        try {

            if (geometry instanceof TPoint)
            {
                if (IndexMappingCache.isMappedAsGeoPoint(support.index(), support.entitiesType(), propertyWithDepth))
                {
                    createESGeoPointIndexAsGeoPointValue(property, json, (TPoint) geometry);
                }
                else if (IndexMappingCache.isMappedAsGeoShape(support.index(), support.entitiesType(), propertyWithDepth))
                {
                    createESGeoPointIndexAsShapeValue(property, json, (TPoint) geometry);
                }
                else new ElasticSearchIndexException("No spatial mapping for property " + propertyWithDepth + " available.");

            } else if (geometry instanceof TMultiPoint)
            {
                createESGeoMultiPointAsShapeIndexValue(property, json, (TMultiPoint) geometry);
            } else if (geometry instanceof TLineString)
            {
                createESGeoLineStringIndexValue(property, json, (TLineString) geometry);
            } else if (geometry instanceof TPolygon)
            {
                createESGeoPolygonAsShapeIndexValue(property, json, (TPolygon) geometry);
            } else if (geometry instanceof TMultiPolygon)
            {
                throw new ElasticSearchIndexException("JJ TODO");
            } else if (geometry instanceof TFeature)
            {
                throw new ElasticSearchIndexException("JJ TODO");
            } else if (geometry instanceof TFeatureCollection)
            {
                throw new ElasticSearchIndexException("JJ TODO");
            } else new ElasticSearchIndexException("Unsupported Geometry : " + geometry.getClass());

        } catch (JSONException _ex) {
            throw new ElasticSearchIndexException("", _ex);
        }
    }


    private static void createESGeoMultiPointAsShapeIndexValue(String property, JSONObject json, TMultiPoint tMultiPoint) throws JSONException {

            Map tMultiPointMap = new HashMap();
            tMultiPointMap.put("type", "multipoint");

            JSONArray points = new JSONArray();
            for (int i = 0; i < tMultiPoint.getNumPoints(); i++) {
                TPoint point = (TPoint) tMultiPoint.getGeometryN(i);
                points.put(new JSONArray().put(point.y()).put(point.x()));
            }

            tMultiPointMap.put("coordinates", points);
            json.put(property, tMultiPointMap);

    }

    private static void createESGeoLineStringIndexValue(String property, JSONObject json, TLineString tLineString) throws JSONException {

            Map tLineStringMap = new HashMap();
            tLineStringMap.put("type", "linestring");

            JSONArray points = new JSONArray();
            for (int i = 0; i < tLineString.getNumPoints(); i++) {
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
    private static void createESGeoPointIndexAsGeoPointValue(String property, JSONObject json, TPoint tPoint) throws JSONException
    {
            Map tPointMap = new HashMap();
            tPointMap.put("lat", tPoint.y());
            tPointMap.put("lon", tPoint.x());

            json.put(property, tPointMap);
    }

    private static void createESGeoPointIndexAsShapeValue(String property, JSONObject json, TPoint tPoint) throws JSONException {
            Map tPointMap = new HashMap();
            // Lat = Y Long = X
            tPointMap.put("type", "point");
            tPointMap.put("coordinates", new JSONArray().put(tPoint.x()).put(tPoint.y()));

            json.put(property, tPointMap);

    }


    private static void createESGeoPolygonAsShapeIndexValue(String property, JSONObject json, TPolygon tPolygon) throws JSONException {


            if (!tPolygon.shell().get().isValid())
                throw new ElasticSearchIndexException("Polygon shell has to be closed - first and last point must match. ");

            Map tPolygonMap = new HashMap();
            tPolygonMap.put("type", "polygon");

            JSONArray coordinates = new JSONArray();


            // shell
            {
                JSONArray shell = new JSONArray();
                for (int i = 0; i < tPolygon.shell().get().getNumPoints(); i++) {
                    JSONArray p = new JSONArray();

                    p.put(tPolygon.shell().get().getPointN(i).x());
                    p.put(tPolygon.shell().get().getPointN(i).y());

                    shell.put(p);
                }
                coordinates.put(shell);
            }


            // wholes
            {
                for (int i = 0; i < tPolygon.holes().get().size(); i++) {
                    JSONArray whole = new JSONArray();
                    // TLinearRing whole = tPolygon.holes().get().get(i);
                    for (int j = 0; j < tPolygon.holes().get().get(i).getNumPoints(); j++) {
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


    private ElasticSearchSpatialIndexer() {
    }

}
