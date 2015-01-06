package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by jj on 19.12.14.
 */
public class GeoShapeBuilder extends AbstractBuilder {

    private static final String DEFAULT_PRECISION = "1m";


    public GeoShapeBuilder(ElasticSearchSupport support) {
        this.support = support;
    }

    public boolean create(String field) {
        try {
            return put(field, createESGeoShapeMapping(field));
        } catch(Exception _ex)
        {
            _ex.printStackTrace();
        }
        return false;
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
                //.field("tree_levels",  "10");
        ;
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

}
