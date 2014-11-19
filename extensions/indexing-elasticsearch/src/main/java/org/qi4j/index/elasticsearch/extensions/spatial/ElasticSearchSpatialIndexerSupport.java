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
import org.json.JSONObject;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.index.elasticsearch.ElasticSearchIndexException;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.util.*;

public final class ElasticSearchSpatialIndexerSupport
{



    public static JSONObject toJSON(ElasticSearchSupport support, TGeometry geometry, String property, String propertyWithDepth, JSONObject json)
    {

        ElasticSearchSpatialIndexerMappingSupport.verifyAndCacheMappings(support, geometry, propertyWithDepth);

        try {

            if (geometry instanceof TPoint) {
                return createESGeoPointIndexValueV2(property, json, (TPoint) geometry);
            }
            else if (geometry instanceof TPolygon) {
                return createESGeoPolygonIndexValue(property, json, (TPolygon) geometry);
            }

        } catch(Exception _ex) {
            throw new ElasticSearchIndexException("", _ex);
        }
        return null;
    }


    private static JSONObject createESGeoPointIndexValue(String property, JSONObject json, TPoint point) throws Exception
    {

        JSONObject jsonPoint = new JSONObject();
        // Format for location is [lon, lat]
        // jsonPoint.put("location", point.coordinates().get().get(1)+ "," + point.coordinates().get().get(0));
                 // .put("type", "geo_point");

        // System.out.println("$lat " +  point.coordinates().get().get(0).coordinate().get().get(0));
        // System.out.println("$lan " +  point.coordinates().get().get(1).coordinate().get().get(0));


        Map pointDef = new HashMap <String, Double>(2);
        // pointDef.put("lat", point.coordinates().get().get(0));
        // pointDef.put("lon", point.coordinates().get().get(1));
        pointDef.put("lat", point.coordinates().get().get(0).coordinate().get().get(0));
        pointDef.put("lon", point.coordinates().get().get(1).coordinate().get().get(0));
        // pointDef.put("type", "geo_point");

        // jsonPoint.put("Location", pointDef);
        // jsonPoint.put(property, pointDef);


        JSONArray coordinates = new JSONArray();
        coordinates.put(point.coordinates().get().get(0).coordinate().get().get(0));
        coordinates.put(point.coordinates().get().get(1).coordinate().get().get(0));

        // jsonPoint.put("coordinates", coordinates);

        // pointDef.put("coordinates",coordinates);
        // pointDef.put("type", "point");


        json.put(property, pointDef);


        // System.out.println("#Indexing TPoint : " + json);

        return jsonPoint;
    }

    private static JSONObject createESGeoPointIndexValueV2(String property, JSONObject json, TPoint tPoint) throws Exception
    {
        JSONObject jsonPoint = new JSONObject();

        // jsonPoint.put("coordinates", )

        Map pointDef = new HashMap();

        pointDef.put("type", "point");



/**
        JSONArray shell = new JSONArray();
        for (int i = 0; i < tPolygon.shell().get().getNumPoints(); i++) {
            JSONArray p = new  JSONArray();

            p.put(tPolygon.shell().get().getPointN(i).X());
            p.put(tPolygon.shell().get().getPointN(i).Y());

            shell.put(p);
        }
*/
        JSONArray coordinates = new JSONArray();

        // coordinates.put(shell);
        // coordinates.put(2d);
        coordinates.put(tPoint.coordinates().get().get(0).coordinate().get().get(0));
        coordinates.put(tPoint.coordinates().get().get(1).coordinate().get().get(0));

        pointDef.put("coordinates", coordinates);

        // jsonPoint.put(property, pointDef);

        json.put(property, pointDef);


        return null;
    }


    private static JSONObject createESGeoPolygonIndexValue(String property, JSONObject json, TPolygon tPolygon) throws Exception
    {
        JSONObject jsonPoint = new JSONObject();

        // jsonPoint.put("coordinates", )

        Map pointDef = new HashMap();

        pointDef.put("type", "polygon");

        JSONArray shell = new JSONArray();
        for (int i = 0; i < tPolygon.shell().get().getNumPoints(); i++) {
            JSONArray p = new  JSONArray();

            p.put(tPolygon.shell().get().getPointN(i).X());
            p.put(tPolygon.shell().get().getPointN(i).Y());

            shell.put(p);
        }

        JSONArray coordinates = new JSONArray();

        coordinates.put(shell);
        // coordinates.put(2d);

        pointDef.put("coordinates", coordinates);

        // jsonPoint.put(property, pointDef);

        json.put(property, pointDef);


        return null;
    }





    private ElasticSearchSpatialIndexerSupport()
    {
    }

}
