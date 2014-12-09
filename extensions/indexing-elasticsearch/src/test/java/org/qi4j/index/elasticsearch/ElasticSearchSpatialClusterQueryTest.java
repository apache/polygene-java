package org.qi4j.index.elasticsearch;

import org.joda.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.geometry.GeometryFactory;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
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
import java.util.Iterator;

import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertNotNull;
import static org.qi4j.api.query.QueryExpressions.*;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Disjoin;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.test.indexing.NameableAssert.verifyUnorderedResults;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;
import static org.qi4j.api.geometry.TGEOM.*;

/**
 * Created by jakes on 2/8/14.
 */
public class ElasticSearchSpatialClusterQueryTest
        extends AbstractSpatialQueryTest {
    private static final File DATA_DIR = new File("build/tmp/es-money-query-test");
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter(DATA_DIR);

    @BeforeClass
    public static void beforeClass_IBMJDK() {
        assumeNoIbmJdk();
    }

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException {
        super.assemble(module);

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
    }

    @Override
    public void setUp()
            throws Exception {
        super.setUp();

        try (UnitOfWork unitOfWork = module.newUnitOfWork()) {

            // Kuala Lumpur

            // Latitude    3.139003
            // Longitude 101.686854

            ValueBuilder<TPolygon> builder = module.newValueBuilder(TPolygon.class);

            TPolygon area = builder.prototype().of
                    (
                            // shell
                            (TLinearRing)module.newValueBuilder(TLinearRing.class).prototype().of
                                    (
                                            module.newValueBuilder(TPoint.class).prototype().x(49.56797785892715).y(10.62652587890625),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.5835615987737).y(10.748062133789062),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.533230478523684).y(10.78857421875),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.484185749507716).y(10.72265625),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.49310663031507).y(10.578460693359375),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.5416968611641).y(10.583267211914062),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.555507284155276).y(10.605239868164062),
                                            module.newValueBuilder(TPoint.class).prototype().x(49.56797785892715).y(10.62652587890625)
                                    )
                    );


            System.out.println("Area " + area);

            City Emskirchen;
            {
                EntityBuilder<City> cityBuilder = unitOfWork.newEntityBuilder(City.class);
                Emskirchen = cityBuilder.instance();
                Emskirchen.name().set("Emskirchen");
                Emskirchen.country().set("Germany");
                Emskirchen.county().set("Bavaria");
                Emskirchen.location().set((TPoint) module.findService(GeometryFactory.class).get()
                        .as2DPoint(49.550881, 10.712809));
                Emskirchen = cityBuilder.newInstance();
                // NameableAssert.trace( kualaLumpur );
                Emskirchen.area().set(area);

            }


            Female annDoe;
            {
                EntityBuilder<FemaleEntity> femaleBuilder = unitOfWork.newEntityBuilder(FemaleEntity.class, "anndoe2");
                annDoe = femaleBuilder.instance();
                annDoe.name().set("Ann Doe 2");
                annDoe.title().set(Person.Title.MRS);
                annDoe.placeOfBirth().set(Emskirchen);
                annDoe.favoritePlaces().put("Emskirchen", Emskirchen);
                annDoe.yearOfBirth().set(1975);
                annDoe.password().set("passwordOfAnnDoe");

                annDoe = femaleBuilder.newInstance();
                NameableAssert.trace(annDoe);
            }

            unitOfWork.complete();

        } catch (Exception _ex) {
            _ex.printStackTrace();
        }

    }


    @Service
    GeometryFactory Geometry;








    @Test
    public void whenQueryUseConversion() throws Exception {
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
                                                100,
                                                TUnit.METER
                                        )
                        ));


        // System.out.println( "*** script01: " + query );
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
    public void whenQueryUsePolygon() throws Exception {


        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(City.class).area(),

                                                ST_GeometryFromText(
                                                        "POLYGON((" +
                                                                "49.56797785892715 10.62652587890625," +
                                                                "49.5835615987737 10.748062133789062," +
                                                                "49.533230478523684 10.78857421875," +
                                                                "49.484185749507716 10.72265625," +
                                                                "49.49310663031507 10.578460693359375," +
                                                                "49.5416968611641 10.583267211914062," +
                                                                "49.555507284155276 10.605239868164062," +
                                                                "49.56797785892715 10.62652587890625))", 1)
                                        )
                        ));


        // System.out.println( "*** script01: " + query );
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
    public void whenQueryUsePolygonDSL() throws Exception {


        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(City.class).area(),

                                                TPOLYGON(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {49.56797785892715, 10.62652587890625},
                                                                                        {49.5835615987737, 10.748062133789062},
                                                                                        {49.533230478523684, 10.78857421875},
                                                                                        {49.484185749507716, 10.72265625},
                                                                                        {49.49310663031507, 10.578460693359375},
                                                                                        {49.5416968611641, 10.583267211914062},
                                                                                        {49.555507284155276, 10.605239868164062},
                                                                                        {49.56797785892715, 10.62652587890625}

                                                                                }
                                                                ).geometry()
                                        )
                        ));


        query.find();

        System.out.println("Found Cities " + query.count());
    }


    @Test
    public void directQuery() {

        ValueBuilder<TPolygon> tPolygonShapeBuilder = module.newValueBuilder(TPolygon.class);
        TPolygon tPolygonShape = tPolygonShapeBuilder.prototype().of
                (
                        // shell
                        (TLinearRing)module.newValueBuilder(TLinearRing.class).prototype().of
                                (
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.56797785892715),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.62652587890625)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.5835615987737),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.748062133789062)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.533230478523684),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.78857421875)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.484185749507716),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.72265625)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.49310663031507),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.578460693359375)   //y
                                                )

                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.5416968611641),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.583267211914062)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.555507284155276),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.605239868164062)   //y
                                                )
                                        ,
                                        module.newValueBuilder(TPoint.class).prototype().of
                                                (
                                                        module.newValueBuilder(Coordinate.class).prototype().of(49.56797785892715),  //x
                                                        module.newValueBuilder(Coordinate.class).prototype().of(10.62652587890625)   //y
                                                )


                                        // ,
                                        // no holes
                                        // null
                                ));


        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);
/**
 Query<City> query = unitOfWork.newQuery(
 qb

 .where(
 ST_Within(templateFor(City.class).location(),

 tPolygonShape
 )));


 // System.out.println( "*** script01: " + query );
 query.find();



 System.out.println("Found Cities 123 " + query.count());
 */

    }


    @Test
    public void whenQueryUseConversion2() throws Exception {
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
                                                100,
                                                TUnit.METER
                                        )
                        ));


        query.find();

        System.out.println("Found Cities " + query.count());

        Iterator<City> cities = query.iterator();

        while (cities.hasNext()) {
            System.out.println("Cities " + cities.next().name());
        }

    }


    @Test
    public void whenSpatialQueryWithNot() throws Exception {

        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(not(
                                        ST_Within
                                                (
                                                        templateFor(City.class).location(),
                                                        ST_GeometryFromText("POINT(49.550881 10.712809)", 1),
                                                        100,
                                                        TUnit.METER
                                                )
                                )
                        ));


        query.find();

        System.out.println("Found Cities " + query.count());

        Iterator<City> cities = query.iterator();

        while (cities.hasNext()) {
            System.out.println("Cities " + cities.next().name());
        }
    }


    @Test
    public void whenST_DisjoinThen() throws Exception {

        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoin(
                                        (
                                                templateFor(City.class).area()),
                                        ST_GeometryFromText("POINT(49.550881 10.712809)", 1),
                                        100
                                )

                        ));


        query.find();

        System.out.println("Found Cities " + query.count());

        Iterator<City> cities = query.iterator();

        while (cities.hasNext()) {
            System.out.println("Cities " + cities.next().name());
        }
    }

    /**
     * QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
     * Person person = templateFor( Person.class );
     * Query<Person> query = unitOfWork.newQuery( qb.where( ge( person.yearOfBirth(), 1973 ) ) );
     * System.out.println( "*** script06: " + query );
     * verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
     *
     * @throws Exception
     */

    //                         .where(ge(templateFor(City.class).location(), "123")));
    @Test
    public void whenSpatialQueryWithLEInvalid() throws Exception {
/**
 QueryBuilder<Person> qbPerson = this.module.newQueryBuilder( Person.class );
 Person person = templateFor( Person.class );
 Query<Person> query = unitOfWork.newQuery( qbPerson.where( ge(person.yearOfBirth(), 1973) ) );
 */

        QueryBuilder<City> qbCity = this.module.newQueryBuilder(City.class);
        City city = templateFor(City.class);
        Query<City> queryCity = unitOfWork.newQuery(qbCity.where(ge(city.location(),

                Geometry.asPoint(
                        Geometry.asCoordinate(3.139003),
                        Geometry.asCoordinate(101.686854)
                        //  Geometry.asCoordinate(10.6108),
                        //  Geometry.asCoordinate(49.5786)
                )

        )));


        queryCity.find();

        System.out.println("Found Cities " + queryCity.count());

        Iterator<City> cities = queryCity.iterator();

        //         Query<Nameable> query = qb.where(
        // ge( person.yearOfBirth(), 1900 ).and( eq( person.placeOfBirth().get().name(), "Penang" ) )

        while (cities.hasNext()) {
            System.out.println("Cities " + cities.next().name());
        }
    }


    @Test
    public void script43_LocalDateTime() {
        QueryBuilder<Person> qb = this.module.newQueryBuilder(Person.class);
        Person person = templateFor(Person.class);
        Query<Person> query = unitOfWork.newQuery(qb.where(
                and(gt(person.localDateTimeValue(), new LocalDateTime("2005-03-04T13:24:35", UTC)),
                        lt(person.localDateTimeValue(), new LocalDateTime("2015-03-04T13:24:35", UTC)))));
        System.out.println("*** script43_LocalDateTime: " + query);

        verifyUnorderedResults(query, "Jack Doe");
    }


}