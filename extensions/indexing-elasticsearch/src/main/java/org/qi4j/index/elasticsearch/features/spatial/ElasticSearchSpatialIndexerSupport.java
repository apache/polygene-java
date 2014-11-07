package org.qi4j.index.elasticsearch.features.spatial;

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

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilder;
import org.json.JSONObject;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.query.grammar.ComparisonSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.index.elasticsearch.ElasticSearchIndexException;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import static org.qi4j.index.elasticsearch.internal.ElasticSearchMappingsCache.MappingsCache;
import static org.qi4j.index.elasticsearch.internal.ElasticSearchMappingsHelper.Mappings;

import java.io.IOException;
import java.util.*;

public final class ElasticSearchSpatialIndexerSupport
{



    public static JSONObject toJSON(ElasticSearchSupport support, TGeometry geometry, String property, String propertyWithDepth, JSONObject json)
    {

        ElasticSearchSpatialMappingSupport.verifyAndCacheMappings(support, geometry, propertyWithDepth);

        try {

            if (geometry instanceof TPoint) {
                return createESGeoPointIndexValue(property, json, (TPoint) geometry);
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

        json.put(property, pointDef);


        // System.out.println("#Indexing TPoint : " + json);

        return jsonPoint;
    }








    private ElasticSearchSpatialIndexerSupport()
    {
    }

}
