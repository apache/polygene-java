package org.qi4j.index.elasticsearch;

import org.joda.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.geometry.GeometryFactory;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESClusterIndexQueryAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialQueryTest;
import org.qi4j.test.indexing.NameableAssert;
import org.qi4j.test.indexing.model.City;
import org.qi4j.test.indexing.model.Female;
import org.qi4j.test.indexing.model.Person;
import org.qi4j.test.indexing.model.entities.FemaleEntity;
import org.qi4j.test.util.DelTreeAfter;

import java.io.File;

import static org.joda.time.DateTimeZone.UTC;
import static org.qi4j.api.query.QueryExpressions.*;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.test.indexing.NameableAssert.verifyUnorderedResults;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

/**
 * Created by jakes on 2/8/14.
 */
public class ElasticSearchSpatialClusterQueryTest
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
        new ESClusterIndexQueryAssembler().
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

    @Override
    public void setUp()
            throws Exception
    {
        super.setUp();

        try( UnitOfWork unitOfWork = module.newUnitOfWork() )
        {

            // Kuala Lumpur

            // Latitude    3.139003
            // Longitude 101.686854

            City kualaLumpur;
            {
                EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder( City.class );
                kualaLumpur = cityBuilder.instance();
                kualaLumpur.name().set( "Kuala Lumpur" );
                kualaLumpur.country().set( "Malaysia" );
                kualaLumpur.county().set( "Some Jaya" );
                kualaLumpur.location().set((TPoint)module.findService(GeometryFactory.class).get()
                        .as2DPoint(49.550881, 10.712809));
                kualaLumpur = cityBuilder.newInstance();
                // NameableAssert.trace( kualaLumpur );

            }


            Female annDoe;
            {
                EntityBuilder<FemaleEntity> femaleBuilder = unitOfWork.newEntityBuilder( FemaleEntity.class, "anndoe" );
                annDoe = femaleBuilder.instance();
                annDoe.name().set( "Ann Doe" );
                annDoe.title().set( Person.Title.MRS );
                annDoe.placeOfBirth().set( kualaLumpur );
                annDoe.favoritePlaces().put("kualaLumpur", kualaLumpur);
                annDoe.yearOfBirth().set( 1975 );
                annDoe.password().set( "passwordOfAnnDoe" );

                annDoe = femaleBuilder.newInstance();
                NameableAssert.trace(annDoe);
            }

            unitOfWork.complete();

        } catch(Exception _ex) {
            _ex.printStackTrace();
        }

    }


    @Service
    GeometryFactory Geometry;

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

        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within(templateFor(City.class).location(),
                                        Geometry.asPoint(
                                                Geometry.asCoordinate(3.139003),
                                                Geometry.asCoordinate(101.686854)
                                                //  Geometry.asCoordinate(10.6108),
                                                //  Geometry.asCoordinate(49.5786)
                                        ))));

        //  GeoJSON.asPoint()


        System.out.println( "*** script01: " + query );
        City city = query.find();

        System.out.println("Found Cities " + query.count());


        System.out.println(city.location());



//        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
//        Person personTemplate = templateFor( Person.class );
//        City placeOfBirth = personTemplate.placeOfBirth().get();
//        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
//        System.out.println( "*** script04: " + query );
//       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }




    @Test
    public void whenQueryForPersonsInACity() {


        QueryBuilder<Person> qb = this.module.newQueryBuilder(Person.class);

        Query<Person> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within(templateFor(Person.class).placeOfBirth().get().location(),
                                        Geometry.asPoint(
                                                Geometry.asCoordinate(3.139003),
                                                Geometry.asCoordinate(101.686854)
                                                //  Geometry.asCoordinate(10.6108),
                                                //  Geometry.asCoordinate(49.5786)
                                        ))));

        query.find();

        System.out.println("Found Persons " + query.count());

    }


    @Test
    public void whenQueryForPersonsThatLikesALocation() {


        QueryBuilder<Person> qb = this.module.newQueryBuilder(Person.class);

        Query<Person> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within(templateFor(Person.class).placeOfBirth().get().location(),
                                        Geometry.asPoint(
                                                Geometry.asCoordinate(3.139003),
                                                Geometry.asCoordinate(101.686854)
                                                //  Geometry.asCoordinate(10.6108),
                                                //  Geometry.asCoordinate(49.5786)
                                        ))));

        query.find();

        System.out.println("Found Persons " + query.count());

    }




    @Test
    public void script01JustQuery()
    {

        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within(templateFor(City.class).location(),
                                        Geometry.asPoint(
                                                Geometry.asCoordinate(49.550881),
                                                Geometry.asCoordinate(10.712809)
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


    @Test
    public void whenQueryUseConversion() throws Exception
    {
        // lat, long
        ST_GeometryFromText("POINT(49.550881 10.712809)", 1);



            QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

            Query<City> query = unitOfWork.newQuery(
                    qb
                            .where(
                                    ST_Within
                                            (
                                                    templateFor(City.class).location(),
                                                    ST_GeometryFromText("POINT(49.550881 10.712809)", 1),
                                                    100
                                            )
                            ));


            // System.out.println( "*** script01: " + query );
            query.find();



        // System.out.println("Found Cities " + query.count());


//        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
//        Person personTemplate = templateFor( Person.class );
//        City placeOfBirth = personTemplate.placeOfBirth().get();
//        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
//        System.out.println( "*** script04: " + query );
//       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }



    @Test
    public void script43_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
                and( gt( person.localDateTimeValue(), new LocalDateTime( "2005-03-04T13:24:35", UTC ) ),
                        lt( person.localDateTimeValue(), new LocalDateTime( "2015-03-04T13:24:35", UTC ) ) ) ) );
        System.out.println( "*** script43_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }



}