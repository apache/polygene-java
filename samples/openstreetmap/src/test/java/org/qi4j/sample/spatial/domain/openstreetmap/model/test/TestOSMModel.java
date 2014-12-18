package org.qi4j.sample.spatial.domain.openstreetmap.model.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.riak.RiakHttpMapEntityStoreAssembler;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESClusterIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extension.spatial.model.entity.SpatialEntity;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.sample.spatial.domain.openstreetmap.model.assembly.OpenStreetMapDomainModelAssembler;
import org.qi4j.sample.spatial.domain.openstreetmap.model.v2.FeatureEntityV2;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.DelTreeAfter;
import org.geojson.*;
import org.qi4j.library.spatial.transformations.geojson.GeoJSONParserV2;

import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.api.query.QueryExpressions.*;


import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.qi4j.api.geometry.*;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.qi4j.sample.spatial.domain.openstreetmap.model.v2.structure.OSM;

import java.util.*;

import static org.qi4j.library.spatial.v2.conversions.TConversions.Convert;

/**
 * Created by jj on 28.11.14.
 */
public class TestOSMModel extends AbstractQi4jTest {

    private static final File DATA_DIR = new File( "build/tmp/es-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }
/**
    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {

        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
                withConfig( config, Visibility.layer ).
                assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
                withData( new File( DATA_DIR, "qi4j-data" ) ).
                withLog( new File( DATA_DIR, "qi4j-logs" ) ).
                withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
                setMetaInfo( override );

        // In-Memory Entity Store
        new EntityTestAssembler().assemble( module );

        new OpenStreetMapDomainModelAssembler().assemble(module);
    }
*/

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException {


        module.entities(SpatialEntity.class);

        // Config module
        ModuleAssembly config = module.layer().module("config");
        new EntityTestAssembler().assemble(config);

        // Index/Query
        new ESClusterIndexQueryAssembler().
                withConfig(config, Visibility.layer).
                assemble(module);
        ElasticSearchConfiguration esConfig = config.forMixin(ElasticSearchConfiguration.class).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set(Boolean.TRUE);

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
                withData(new File(DATA_DIR, "qi4j-data")).
                withLog(new File(DATA_DIR, "qi4j-logs")).
                withTemporary(new File(DATA_DIR, "qi4j-temp"));
        module.services(FileConfigurationService.class).
                setMetaInfo(override);

        // In-Memory Entity Store
        // new EntityTestAssembler().assemble( module );

        /** RIAK */
        ModuleAssembly configRiak = module.layer().module( "configRiak" );
        new EntityTestAssembler().assemble( configRiak );
        new OrgJsonValueSerializationAssembler().assemble( module );
        // START SNIPPET: assembly
        new RiakHttpMapEntityStoreAssembler().withConfig( configRiak, Visibility.layer ).assemble( module );
        /** +++ */



        new OpenStreetMapDomainModelAssembler().assemble(module);
    }

    // @Before
    public void setup() throws Exception
    {
        int totalCounter = 0;
        final int numOfLines = 100000; // 140000;
        int counter    = 0;

        int commitCounter = 0;

        Map<String, List<String>> properties;
        TGeometry tGeometry = null;

        JsonParser parser = GeoJSONParserV2.source(new BufferedInputStream(this.getClass().getClassLoader().getResource("data/bavaria/osm-pois").openStream())).build();

        JsonToken token;

        module.newUnitOfWork();

        while ((token = parser.nextToken()) != null) {

            totalCounter++;

            properties = new HashMap<>();

          if (counter++ > numOfLines)
          {
              break;
          }


            if (commitCounter++ > 20000)
            {
                System.out.println("Processed ==> " + totalCounter);
                module.currentUnitOfWork().complete();
                module.newUnitOfWork();
                commitCounter = 0;
            }

            switch (token) {
                case START_OBJECT:
                    JsonNode node = parser.readValueAsTree();


                    // System.out.println("== > " + node.get("id"));

                    JsonNode osm = node.get("categories").get("osm");

                    List<String> list = new ArrayList<>();

                    if (osm.isArray()) {
                        for (final JsonNode property : osm) {
                           // System.out.println(property);
                            list.add(property.asText());
                        }
                    }

                    properties.put("osm", list);

                    if ("Point".equals(node.get("geometry").get("type").asText())) {
                        Point point = new ObjectMapper().readValue(node.get("geometry").toString(), Point.class);
                        TPoint tPoint = (TPoint)Convert(module).from(point).toTGeometry();
                        // System.out.println(tPoint);
                        tGeometry = tPoint;
                    }
                    else if ("LineString".equals(node.get("geometry").get("type").asText())) {
                        LineString lineString = new ObjectMapper().readValue(node.get("geometry").toString(), LineString.class);
                        TLineString tLineString = (TLineString)Convert(module).from(lineString).toTGeometry();
                        tGeometry = tLineString;

                    }
                    else if ("Polygon".equals(node.get("geometry").get("type").asText())) {
                        Polygon polygon = new ObjectMapper().readValue(node.get("geometry").toString(), Polygon.class);
                        TPolygon tPolygon = (TPolygon)Convert(module).from(polygon).toTGeometry();
                        tGeometry = tPolygon;
                    }


                    break;
            }

            if (tGeometry != null) {
                TFeature osmFeature = TFeature(module).of(tGeometry).geometry().withProperties(properties);
                // System.out.println(osmFeature);
                OSM.Repository.$(module).createFeature(osmFeature);
            }




        }

        module.currentUnitOfWork().complete();

    }

    @Test
    public void test() throws Exception
    {
        // OSM.Repository.$(module).createFeature()
        QueryBuilder<FeatureEntityV2> qb = this.module.newQueryBuilder(FeatureEntityV2.class);


        Query<FeatureEntityV2> query =  module.newUnitOfWork().newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(FeatureEntityV2.class).osmpoint(),
                                                TPoint(module).x(50.0599101).y(11.1522125).geometry(),
                                                100000,
                                                TUnit.KILOMETER
                                        )
                        )

        );


        System.out.println(query.count());

        query.maxResults(1000);

        Iterator<FeatureEntityV2> iterator = query.iterator();

        while(iterator.hasNext())
        {
            FeatureEntityV2 point = iterator.next();
            System.out.println(point.properties());
        }

        module.currentUnitOfWork().complete();
    }


    @Test
    public void whenThirstyThenFindDrinkingWaterInMuenichV2() throws Exception
    {

        TPoint muenich = TPoint(module).x(48.1169263).y(11.5763754).geometry();

        QueryBuilder<FeatureEntityV2> qb = this.module.newQueryBuilder(FeatureEntityV2.class);

        Query<FeatureEntityV2> query =  module.newUnitOfWork().newQuery(
                qb
                        .where(or(
                                and(
                                        // Where in OSM Points
                                        ST_Within
                                                (
                                                        templateFor(FeatureEntityV2.class).osmpoint(),
                                                        muenich,
                                                        100,
                                                        TUnit.KILOMETER
                                                )
                                        ,
                                        // What
                                        QueryExpressions.contains(templateFor(FeatureEntityV2.class).properties(), "amenity:drinking_water")
                                )
                                ,
                                and(
                                        // Where in OSM Ways
                                        ST_Within
                                                (
                                                        templateFor(FeatureEntityV2.class).osmway(),
                                                        muenich,
                                                        100,
                                                        TUnit.KILOMETER
                                                )
                                        ,
                                        // What
                                        QueryExpressions.contains(templateFor(FeatureEntityV2.class).properties(), "amenity:drinking_water")
                                )

                        ))
        );


        System.out.println("Number of Results " + query.count());

        Iterator<FeatureEntityV2> iterator = query.iterator();

        while(iterator.hasNext())
        {
            FeatureEntityV2 point = iterator.next();
            System.out.println(point.properties() + " at lon "  + point.osmpoint().get().y() + " lat " + point.osmpoint().get().x());
        }

        module.currentUnitOfWork().complete();
    }

    @Test
    public void whenThirstyThenFindDrinkingWaterInMuenich() throws Exception
    {

        TPoint muenich = TPoint(module).x(48.1169263).y(11.5763754).geometry();

        QueryBuilder<FeatureEntityV2> qb = this.module.newQueryBuilder(FeatureEntityV2.class);

        Query<FeatureEntityV2> query =  module.newUnitOfWork().newQuery(
                qb
                        .where(and(
                                        // Where
                                        ST_Within
                                                (
                                                        templateFor(FeatureEntityV2.class).osmpoint(),
                                                        muenich,
                                                        100,
                                                        TUnit.KILOMETER
                                                )
                                        ,
                                        // What
                                        QueryExpressions.contains(templateFor(FeatureEntityV2.class).properties(), "amenity:drinking_water")
                                )
                        )
        ).orderBy(templateFor(FeatureEntityV2.class).osmpoint(), muenich, OrderBy.Order.ASCENDING);


        System.out.println("Number of Results " + query.count());

        Iterator<FeatureEntityV2> iterator = query.iterator();

        while(iterator.hasNext())
        {
            FeatureEntityV2 point = iterator.next();
            System.out.println(point.properties() + " at lon "  + point.osmpoint().get().y() + " lat " + point.osmpoint().get().x());
        }

        module.currentUnitOfWork().complete();
    }


    @Test
    public void naivePerformanceTest() throws Exception
    {
        final int iterations = 1000;

        long start = System.currentTimeMillis();

        for (int i = 0; i <= iterations; i++)
        {
            whenThirstyThenFindDrinkingWaterInMuenich();
        }

        long end = System.currentTimeMillis();

        long duration = (end - start);

        System.out.println(duration);

        System.out.println(duration / iterations + " ms per query");
    }

    /**
    @Test
    public void testCreatingSpatialFeature() throws Exception
    {
        Map<String, String> properties = new HashMap<>();
        properties.put("note", "80339 München");


        module.newUnitOfWork();
        OSM.Repository.$(module).createFeature(
                TFEATURE(module).of(TPOINT(module).x(11.5429443d).y(48.1427636d).geometry()).geometry(),
                properties
        );
        module.currentUnitOfWork().complete();

       //  System.out.println("foo");
    }
*/
    @Test
    public void testQueryForSpatialFeature() throws Exception
    {
/**
        testCreatingSpatialFeature();

        QueryBuilder<FeatureEntity> qb = this.module.newQueryBuilder(FeatureEntity.class);

        Query<FeatureEntity> query = module.newUnitOfWork().newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(FeatureEntity.class).feature(),
                                                TPOINT(module).x(11.5429443d).y(48.1427636d).geometry()
                                        )
                        ));


        query.find();

        System.out.println(query.count());

        module.currentUnitOfWork().complete();
*/
        //  System.out.println("foo");
    }

}
