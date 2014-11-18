package org.qi4j.library.spatial.parser.geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Iterator;


/**
 * Created by jakes on 2/23/14.
 */
public class GeoJSONParser {

    public void parse() throws Exception {

        BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/home/jakes/Projects/QI4J/Spatial/qi4j-sdk/libraries/spatial/src/test/resources/topo/geojson/germany/bavaria.neustadt.geojson"));

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(inputstream, FeatureCollection.class);

        System.out.println("Found num of features " + featureCollection.getFeatures().size());

        Iterator<Feature> features = featureCollection.getFeatures().iterator();

        while (features.hasNext()) {
            Feature feature = features.next();

            if (feature.getGeometry() instanceof Point) {
               System.out.println("Processing Ppint");
            }


            System.out.println(feature.getGeometry().getClass() );
        }

    }

    public static void main(String [] args)
    {
        GeoJSONParser geoJSONParser = new GeoJSONParser();
        try {
        geoJSONParser.parse();
        } catch(Exception _ex) {
            _ex.printStackTrace();
        }
    }
}
