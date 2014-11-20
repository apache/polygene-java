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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.io.IOException;
import java.util.StringTokenizer;

import static org.qi4j.index.elasticsearch.extensions.spatial.mapping.ElasticSearchMappingsCache.MappingsCache;
import static org.qi4j.index.elasticsearch.extensions.spatial.mapping.ElasticSearchMappingsHelper.Mappings;

public final class ElasticSearchSpatialIndexerMappingSupport {

    private static final String DEFAULT_PRECISION = "1m";
    private static final boolean DEFAULT_GEOHASH_SUPPORT = true;




    public static void verifyAndCacheMappings(ElasticSearchSupport support, TGeometry geometry, String propertyWithDepth) {

        try {


            if (!MappingsCache().exists(propertyWithDepth)) {

                if (Mappings(support).onIndex(support.index()).andType(support.entitiesType()).existsFieldMapping(propertyWithDepth)) {
                    // if (Mappings(support).onIndex(support.index()).andType(support.entitiesType()).existsFieldMapping (propertyWithDepth)) {
                    MappingsCache().put(propertyWithDepth);
                    System.out.println("#### Added a mapping available on the Server but not yet in the in-memory cache : " + propertyWithDepth);
                } else {


                    String _smJson = null;


                    if (geometry instanceof TPoint) // || spatialValueType.type().get().equalsIgnoreCase("point"))
                    {
                        _smJson = createESGeoPointMapping(propertyWithDepth);
                    } else if (geometry instanceof TGeometry) {
                        // JJ TODO
                        _smJson = createESGeoShapeMapping(propertyWithDepth);
                        // System.out.println("TLineString not supported " + spatialValueType);

                    }
                    /**
                    else if (geometry instanceof TPolygon) {
                        // System.out.println("TPolygon not supported " + spatialValueType);

                    } else if (geometry instanceof TGeometry) {
                        // System.out.println("TGeometry..");
                        throw new UnsupportedOperationException(TGeometry.class.getName() + " not supported. Please use a concrete geometry type e.g. " + TPoint.class.getName());
                    } else {
                        // System.out.println("TOTher not supported " + spatialValueType);
                    }

                     **/

                    Mappings(support).onIndex(support.index()).andType(support.entitiesType()).addFieldMappings(propertyWithDepth, _smJson);
                    System.out.println("#### Adding new spatial mappings to the Index : " + propertyWithDepth);

                }
            }
        } catch (Exception _ex) {
            _ex.printStackTrace();
        }
    }


    // https://www.found.no/foundation/elasticsearch-mapping-introduction/
    private static String createESGeoPointMapping(String property) throws IOException {

        String valueType = "qi4j_entities"; // TODO JJ hack here

        // System.out.println("############## Property Tree" + property);

        XContentBuilder qi4jRootType = XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities"); // .startObject("properties");

        StringTokenizer tokenizer1 = new StringTokenizer(property, ".");
        String propertyLevel1;
        while (tokenizer1.hasMoreTokens()) {
            propertyLevel1 = tokenizer1.nextToken();
            // System.out.println("--> start level " + propertyLevel1);
            qi4jRootType.startObject("properties").startObject(propertyLevel1);
        }


        qi4jRootType.field("type", "geo_shape") // geo_point
                // .field("lat_lon", true)
                        // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
                .field("precision", DEFAULT_PRECISION);
                // .field("validate_lat", "true")
                //.field("validate_lon", "true");

        StringTokenizer tokenizer2 = new StringTokenizer(property, ".");
        String propertyLevel2;
        while (tokenizer2.hasMoreTokens()) {
            propertyLevel2 = tokenizer2.nextToken();
            // System.out.println("--> end level " + propertyLevel2);
            // qi4jRootType.startObject(propertyLevel1);
            qi4jRootType.endObject();
        }

/**
 return XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities")// valueType)
 .startObject("properties").startObject(property)
 .field("type", "geo_point")
 .field("lat_lon", true)
 // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
 .field("precision", DEFAULT_PRECISION)
 .field("validate_lat", "true")
 .field("validate_lon", "true")
 .endObject().endObject()
 .endObject().endObject().string();
 */

        qi4jRootType.endObject().endObject().endObject();

        // System.out.println("qi4jRootType.toString() " + qi4jRootType.string());

        return qi4jRootType.string();
    }


    private static String createESGeoShapeMapping(String property) throws IOException {

        String valueType = "qi4j_entities"; // TODO JJ hack here

        // System.out.println("############## Property Tree" + property);

        XContentBuilder qi4jRootType = XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities"); // .startObject("properties");

        StringTokenizer tokenizer1 = new StringTokenizer(property, ".");
        String propertyLevel1;
        while (tokenizer1.hasMoreTokens()) {
            propertyLevel1 = tokenizer1.nextToken();
            // System.out.println("--> start level " + propertyLevel1);
            qi4jRootType.startObject("properties").startObject(propertyLevel1);
        }


        qi4jRootType.field("type", "geo_shape")
                // .field("lat_lon", true)
                // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
                .field("precision", DEFAULT_PRECISION)
                .field("tree", "quadtree")
                .field("tree_levels",  "20");
        //.field("validate_lat", "true")
        //.field("validate_lon", "true");

        StringTokenizer tokenizer2 = new StringTokenizer(property, ".");
        String propertyLevel2;
        while (tokenizer2.hasMoreTokens()) {
            propertyLevel2 = tokenizer2.nextToken();
            // System.out.println("--> end level " + propertyLevel2);
            // qi4jRootType.startObject(propertyLevel1);
            qi4jRootType.endObject();
        }

/**
 return XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities")// valueType)
 .startObject("properties").startObject(property)
 .field("type", "geo_point")
 .field("lat_lon", true)
 // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
 .field("precision", DEFAULT_PRECISION)
 .field("validate_lat", "true")
 .field("validate_lon", "true")
 .endObject().endObject()
 .endObject().endObject().string();
 */

        qi4jRootType.endObject().endObject().endObject();

        // System.out.println("qi4jRootType.toString() " + qi4jRootType.string());

        return qi4jRootType.string();


    }


    private ElasticSearchSpatialIndexerMappingSupport() {
    }

}
