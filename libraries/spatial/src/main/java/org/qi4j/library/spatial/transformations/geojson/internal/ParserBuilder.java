package org.qi4j.library.spatial.transformations.geojson.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class ParserBuilder
{

    private InputStream source;
    private FeatureCollection featureCollection;
    private Iterator<Feature> features;

    public ParserBuilder(InputStream source)
    {
        this.source = source;
    }

/**
    public void parse( Class valueType) throws Exception
    {
        FeatureCollection featureCollection =
                new ObjectMapper().readValue(source, FeatureCollection);
    }
*/

    public ParserBuilder parse() throws Exception
    {
        // parse(FeatureCollection.class);
        // FeatureCollection featureCollection =



        this.featureCollection = new ObjectMapper().readValue(source, FeatureCollection.class);

        this.features = featureCollection.getFeatures().iterator();

        return this;
    }

    public JsonParser  build() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = mapper.getJsonFactory();
        JsonParser jp = jsonFactory.createJsonParser(source);

        return jp;
        /**
        JsonFactory f = new JsonFactory();
        JsonParser jp  = f.createParser(source);
        // JsonParser jp = f.createJsonParser(new File("user.json"));

        return jp;
        */

    }



    public List<Feature>  getValues()
    {
        return this.featureCollection.getFeatures();
    }






}