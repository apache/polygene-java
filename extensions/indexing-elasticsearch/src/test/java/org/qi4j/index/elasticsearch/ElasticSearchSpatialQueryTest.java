package org.qi4j.index.elasticsearch;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.geojson.Feature;
// import org.geojson.FeatureCollection;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.geometry.GeometryFactory;
import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
// import org.qi4j.library.spatial.transformator.GeoJsonTransformator;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialQueryTest;
import org.qi4j.test.indexing.model.City;
import org.qi4j.test.util.DelTreeAfter;

import java.io.File;

import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

/**
 * Created by jakes on 2/8/14.
 */
public class ElasticSearchSpatialQueryTest
        extends AbstractSpatialQueryTest
{






    private static final File DATA_DIR = new File( "build/tmp/es-money-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );


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
    }

    @Service
    GeometryFactory Geometry;

/**
    // @Before
    public void setup()
    {


       //  UnitOfWork uow = module.newUnitOfWork();
        try
        {


            BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/home/jakes/Projects/QI4J/Spatial/qi4j-sdk/libraries/spatial/src/test/resources/topo/geojson/germany/bavaria.neustadt.geojson"));

            //  BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/media/HDD_002/spatial/OpenStreetMap/geofabrik.de/nuenberg.geojson"));

            FeatureCollection featureCollection =
                    new ObjectMapper().readValue(inputstream, FeatureCollection.class);

            System.out.println("Found num of features " + featureCollection.getFeatures().size());

            int count = 0;

            Iterator<Feature> features = featureCollection.getFeatures().iterator();

            while (features.hasNext()) {

                count++;

                Feature feature = features.next();

                TFeature tFeature =  GeoJsonTransformator.withGeometryFactory(Geometry).transformGeoFeature(feature);
                if (tFeature != null)
                {
                    // System.out.println(tFeature);

                   // if (tFeature.asGeometry().type().get().equalsIgnoreCase("point"))
                    if (tFeature.asGeometry() instanceof TPoint)
                    {
                        UnitOfWork uow1 = module.newUnitOfWork();
                            MapFeature mapFeature = from(tFeature);
                         System.out.println(mapFeature);
                        uow1.complete();

                    }
                }

            }



        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            // log.error( ex.getMessage(), ex );
            // throw ex;
        }

    }

 */

    @Test
    public void script0()  throws Exception {

        // UnitOfWork uow = module.newUnitOfWork();
        try
        {

          System.out.println("Test..");
            // setup();


        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            // log.error( ex.getMessage(), ex );
            throw ex;
        }
        finally
        {
          //   uow.discard();
        }

        // uow.complete();

    }

    @Test
    public void script00()
    {
//        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
//                where( eq( templateFor( Person.class ).money(),
//                        Money.of( CurrencyUnit.USD, 100 ) ) ) );
//
//        verifyUnorderedResults( query, "Joe Doe" );
       //  setup();

        QueryBuilder<MapFeature> qb = this.module.newQueryBuilder(MapFeature.class);

        Query<MapFeature> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within(templateFor(MapFeature.class).geometry1(),
                                        Geometry.asPoint(
                                                Geometry.asCoordinate(10.6108),
                                                Geometry.asCoordinate(49.5786)
                                        ))));

        //  GeoJSON.asPoint()


        System.out.println( "*** script01: " + query );
        query.find();

        System.out.println(query.count());


//        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
//        Person personTemplate = templateFor( Person.class );
//        City placeOfBirth = personTemplate.placeOfBirth().get();
//        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
//        System.out.println( "*** script04: " + query );
//       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }


    @Test
    public void script01()
    {

        try( UnitOfWork unitOfWork = module.newUnitOfWork() )
        {

            City kualaLumpur;
            {
                EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
                kualaLumpur = cityBuilder.instance();
                kualaLumpur.name().set( "Kuala Lumpur" );
                kualaLumpur.country().set( "Malaysia" );
                kualaLumpur.county().set( "Some Jaya" );
                kualaLumpur.location().set((TPoint)module.findService(GeometryFactory.class).get()
                        .as2DPoint( 3.173425,101.675720));
                kualaLumpur = cityBuilder.newInstance();
               // NameableAssert.trace( kualaLumpur );
                unitOfWork.complete();
            }

        } catch(Exception _ex) {
            _ex.printStackTrace();
        }




        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within(templateFor(City.class).location(),
                                        Geometry.asPoint(
                                                Geometry.asCoordinate(101.675520),
                                                Geometry.asCoordinate(3.173225)
                                               //  Geometry.asCoordinate(10.6108),
                                               //  Geometry.asCoordinate(49.5786)
                                        ))));

        //  GeoJSON.asPoint()


        System.out.println( "*** script01: " + query );
        query.find();

        System.out.println("Found Cities " + query.count());


//        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
//        Person personTemplate = templateFor( Person.class );
//        City placeOfBirth = personTemplate.placeOfBirth().get();
//        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
//        System.out.println( "*** script04: " + query );
//       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }


    private MapFeature from(TFeature tFeature)

    {


        MapFeature feature;
        EntityBuilder<MapFeature> featureBuilder = unitOfWork.newEntityBuilder( MapFeature.class );
        feature = featureBuilder.instance();

        feature.geometry1().set(tFeature.asGeometry());
        feature.properties().set(tFeature.asProperties());

        feature = featureBuilder.newInstance();

        return feature;

    }

}