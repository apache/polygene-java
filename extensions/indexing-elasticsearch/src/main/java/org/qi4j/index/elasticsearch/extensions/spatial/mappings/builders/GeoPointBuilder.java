package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by jj on 19.12.14.
 */
public class GeoPointBuilder extends AbstractBuilder {

    private static final String DEFAULT_PRECISION = "1m";


    public GeoPointBuilder(ElasticSearchSupport support) {
        this.support = support;
    }


    public boolean create(String field) {
        try {
            return put(field, createGeoPointMapping(field));
        } catch(Exception _ex)
        {
            _ex.printStackTrace();
        }
        return false;
    }


    private static String createGeoPointMapping(String field) throws IOException {


        XContentBuilder qi4jRootType = XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities");

        StringTokenizer tokenizer1 = new StringTokenizer(field, ".");
        String propertyLevel1;
        while (tokenizer1.hasMoreTokens()) {
            propertyLevel1 = tokenizer1.nextToken();
            // System.out.println("--> start level " + propertyLevel1);
            qi4jRootType.startObject("properties").startObject(propertyLevel1);
        }

        qi4jRootType.field("type", "geo_point") // geo_point
                // .field("lat_lon", true)
                // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
                .field("precision", DEFAULT_PRECISION)
                .field("lat_lon", true);

        StringTokenizer tokenizer2 = new StringTokenizer(field, ".");
        String propertyLevel2;
        while (tokenizer2.hasMoreTokens()) {
            propertyLevel2 = tokenizer2.nextToken();

            qi4jRootType.endObject();
        }

        qi4jRootType.endObject().endObject().endObject();


        return qi4jRootType.string();

    }

}
