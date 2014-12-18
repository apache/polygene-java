package org.qi4j.library.spatial.parser.geojson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.topo.GeoJSONSwissLakes2013;
import org.qi4j.library.spatial.transformations.GeoJsonTransformator;
import org.qi4j.library.spatial.transformations.geojson.GeoJSONParserV2;
import org.qi4j.library.spatial.transformations.geojson.internal.ParserBuilder;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

import static org.qi4j.library.spatial.v2.conversions.TConversions.Convert;

/**
 * Created by jakes on 2/7/14.
 */

/**
 *     http://stackoverflow.com/questions/4619306/list-arrary-double-entering-values
 *     http://worldwind31.arc.nasa.gov/svn/trunk/WorldWind/src/gov/nasa/worldwindx/examples/GeoJSONLoader.java
 *
 *
 */
public class SimpleGeoJSONParser extends AbstractQi4jTest {

    private Visibility visibility = Visibility.module;



    @Rule
    @SuppressWarnings( "PublicField" )
    public TestName testName = new TestName();
    private Logger log;

    @Before
    public void before()
    {
        log = LoggerFactory.getLogger(testName.getMethodName());
        module.injectTo( this );
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( OrgJsonValueSerializationService.class ).
                visibleIn( visibility ).
                taggedWith(ValueSerialization.Formats.JSON);

        // internal values
        module.values( Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class,TLineString.class, TPolygon.class, TFeature.class);


    }

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;



    @Test
    public void parse()  throws Exception {

        ParserBuilder parser = GeoJSONParserV2.source(new BufferedInputStream(this.getClass().getClassLoader().getResource("data/munich.geojson").openStream())).parse();
        List<Feature> features = parser.getValues();
        System.out.println("Found features " + features.size());

        for (int i = 0; i < features.size(); i++)
        {
            Feature feature = features.get(i);
            System.out.println(feature.getGeometry());

            TGeometry tGeometry = GeoJSONParserV2.transform(module).from(feature).transform();
            System.out.println(i +  "  " + tGeometry + " " + tGeometry.getType());
        }
    }

    @Test
    public void parsePerformanceTest()  throws Exception {

        // ParserBuilder parser =

        JsonParser parser = GeoJSONParserV2.source(new BufferedInputStream(this.getClass().getClassLoader().getResource("data/bavaria/osm-ways").openStream())).build();

        JsonToken token;

        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case START_OBJECT:
                    JsonNode node = parser.readValueAsTree();
                    // ... do something with the object
                    // System.out.println("Read object: " + node.toString());
                    // System.out.println(node.get("type"));
                    // FeatureCollection.class
                    // System.out.println(node.toString());
                    // System.out.println(node.get("geometry"));

                    // if (node.get("geometry").get("type"))
                    // System.out.println(node.get("geometry").get("type"));
                    // System.out.println(node.get("geometry").get("type"));

                    // System.out.println(node.get("geometry").get("type").asText());

                    System.out.println("== > " + node.get("id"));

                    //  System.out.println(node.get("categories").get("osm").ar);

                    JsonNode osm = node.get("categories").get("osm");

                    if (osm.isArray()) {
                        for (final JsonNode property : osm) {
                            System.out.println(property);
                        }
                    }

                    if ("Point".equals(node.get("geometry").get("type").asText())) {
                        Point point = new ObjectMapper().readValue(node.get("geometry").toString(), Point.class);
                        TPoint tPoint = (TPoint)Convert(module).from(point).toTGeometry();
                        // System.out.println(tPoint);
                    }
                    else if ("LineString".equals(node.get("geometry").get("type").asText())) {
                        LineString lineString = new ObjectMapper().readValue(node.get("geometry").toString(), LineString.class);
                        TLineString tLineString = (TLineString)Convert(module).from(lineString).toTGeometry();
                        // System.out.println(tLineString);

                    }
                    else if ("Polygon".equals(node.get("geometry").get("type").asText())) {
                        Polygon polygon = new ObjectMapper().readValue(node.get("geometry").toString(), Polygon.class);
                        TPolygon tPolygon = (TPolygon)Convert(module).from(polygon).toTGeometry();
                        // System.out.println(tPolygon);
                        // System.out.println(tPolygon);
                        // System.out.println(tPolygon.shell().get().isValid());
                    }


                    break;
            }
        }
    }

        // parser.nextToken();


        @Test
        public void testFoo() throws Exception
        {

            InputStream source = new BufferedInputStream(this.getClass().getClassLoader().getResource("data/osm-pois").openStream());
            GeoJsonObject object = new ObjectMapper().readValue(source, FeatureCollection.class);
/**
            if (object instanceof Point) {
            }

            else if (object instanceof Polygon) {
            }
*/



        }



/**
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            parser.
            String fieldname = parser.getCurrentName();
            JsonToken token = parser.nextToken(); // move to value, or START_OBJECT/START_ARRAY
            System.out.println(fieldname);
            if ("type".equals(fieldname)) {
               // JsonToken token = parser.getCurrentToken();
              System.out.println("-- > " + token);

                new ObjectMapper().readV readValue(source, FeatureCollection.class);
            }
            }
*/

                        /**
        List<Feature> features = parser.getValues();
        System.out.println("Found features " + features.size());

        for (int i = 0; i < features.size(); i++)
        {
            Feature feature = features.get(i);
            System.out.println(feature.getGeometry());

            TGeometry tGeometry = GeoJSONParserV2.transform(module).from(feature).transform();
            System.out.println(i +  "  " + tGeometry + " " + tGeometry.getType());
        }
         */


   @Test
   public void foo()  throws Exception {

       UnitOfWork uow = module.newUnitOfWork();
       try
       {

           // ClassLoader.getSystemResource(name) && null == this.getClass().getClassLoader().getResource(name))

           // this.getClass().getClassLoader().getResource("name")

           BufferedInputStream inputstream = new BufferedInputStream( this.getClass().getClassLoader().getResource("data/munich.geojson").openStream());

           // BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/home/jakes/Projects/QI4J/Spatial/qi4j-sdk/libraries/spatial/src/test/resources/topo/geojson/germany/bavaria.neustadt.geojson"));

          //  BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/media/HDD_002/spatial/OpenStreetMap/geofabrik.de/nuenberg.geojson"));

           FeatureCollection featureCollection =
                   new ObjectMapper().readValue(inputstream, FeatureCollection.class);

           System.out.println("Found num of features " + featureCollection.getFeatures().size());

           int count = 0;

           Iterator<Feature> features = featureCollection.getFeatures().iterator();

           while (features.hasNext()) {

               count++;

               Feature feature = features.next();

              //  TFeature tFeature =  GeoJsonTransformator.withGeometryFactory(Geometry).transformGeoFeature(feature);
              // System.out.println("-> " + tFeature);

           }



       }
       catch( Exception ex )
       {
           ex.printStackTrace();
           // log.error( ex.getMessage(), ex );
           throw ex;
       }
       finally
       {
           uow.discard();
       }


   }






    @Test
    public void testfoo() throws Exception {

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(GeoJSONSwissLakes2013.SWISS_LAKES , FeatureCollection.class);



//        File file = new File("/media/HDD_002/spatial/usa-lines.geojson");
//        FileInputStream fis = new FileInputStream(file);
//
//
//         FeatureCollection featureCollection1 =
//                new ObjectMapper().readValue(fis , FeatureCollection.class);



    }





}
