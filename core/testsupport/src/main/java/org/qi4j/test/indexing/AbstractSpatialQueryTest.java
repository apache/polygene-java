package org.qi4j.test.indexing;

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.indexing.model.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;

/**
 * Created by jakes on 2/8/14.
 */
public class AbstractSpatialQueryTest
                extends AbstractAnyQueryTest
{

    private final String CRS_EPSG_4326 = "EPSG:4326";

    public interface MapFeature

    {


        @Optional
        Property<Map<String, Object>> properties();

        // @Optional
        Property<TGeometry> geometry1();

    }


    public interface MapFeatureEntity
            extends MapFeature, EntityComposite
    {
    }


    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );
        module.entities(MapFeatureEntity.class);

        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);
        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS_EPSG_4326);

    }



    // @Test
    public void whenQueryUseConversion() throws Exception
    {
        // lat, long
        ST_GeometryFromText("POINT(49.550881 10.712809)");



        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);

        Query<City> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(City.class).location(),
                                                ST_GeometryFromText("POINT(49.550881 10.712809)"),
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



    // @Test
    public void script11()
    {
//        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
//                where( eq( templateFor( Person.class ).money(),
//                        Money.of( CurrencyUnit.USD, 100 ) ) ) );
//
//        verifyUnorderedResults( query, "Joe Doe" );


        QueryBuilder<City> qb = this.module.newQueryBuilder( City.class );
        City cityTemplate = templateFor( City.class );
        //  Query<City> query = unitOfWork.newQuery( qb.where( eq( cityTemplate.location(), "Kuala Lumpur" ) ) );

        ValueBuilder<TPolygon> builder = module.newValueBuilder(TPolygon.class);
        TPolygon proto = builder.prototype();

        List<List<List<Double>>> coordinates = new ArrayList<List<List<Double>>>();

        List<List<Double>> a = new ArrayList<List<Double>>();
        a.add(new ArrayList<Double>());
        a.add(new ArrayList<Double>());
        a.add(new ArrayList<Double>());
        coordinates.add(a);

        List<List<Double>> b = new ArrayList<List<Double>>();
        b.add(new ArrayList<Double>());
        b.add(new ArrayList<Double>());
        a.add(new ArrayList<Double>());
        coordinates.add(b);

        // new ArrayList<List<Double>>().add(new ArrayList<Double>());



        coordinates.get(0).get(0).add(0.0);
        coordinates.get(0).get(1).add(0.1);
        coordinates.get(0).get(2).add(0.2);
        coordinates.get(0).get(3).add(0.3);

        coordinates.get(1).get(1).add(1.1);


       // proto.coordinates().set(coordinates);


        //Double lat =  3.138722;  // 3.138722;// Double.parseDouble(query.nextToken());
        //Double lon =  101.386849; // Double.parseDouble(query.nextToken());


        // coordinates.add(3.138722);
        // coordinates.add(101.386849);




//        proto.coordinates().set(coordinates);
//
//        Query<City> query = unitOfWork.newQuery(
//                qb
//                        .where(                        ST_Within(cityTemplate.location(), builder.newInstance())
//
//                        ));
//
//        System.out.println( "*** script01: " + query );
//        query.find();
//
//        System.out.println(query.count());


//        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
//        Person personTemplate = templateFor( Person.class );
//        City placeOfBirth = personTemplate.placeOfBirth().get();
//        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
//        System.out.println( "*** script04: " + query );
//       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    // @Test
    public void script2()
    {
//        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
//                where( eq( templateFor( Person.class ).money(),
//                        Money.of( CurrencyUnit.USD, 100 ) ) ) );
//
//        verifyUnorderedResults( query, "Joe Doe" );


//        QueryBuilder<City> qb = this.module.newQueryBuilder( City.class );
//        City cityTemplate = templateFor( City.class );
//        //  Query<City> query = unitOfWork.newQuery( qb.where( eq( cityTemplate.location(), "Kuala Lumpur" ) ) );
//
//        ValueBuilder<TGeomPoint> builder = module.newValueBuilder( TGeomPoint.class );
//        TGeomPoint proto = builder.prototype();
//        List<Double> coordinates = new ArrayList<Double>();
//
//        //Double lat =  3.138722;  // 3.138722;// Double.parseDouble(query.nextToken());
//        //Double lon =  101.386849; // Double.parseDouble(query.nextToken());
//
//
//        coordinates.add(3.138722);
//        coordinates.add(101.386849);
//
//        proto.coordinates().set(coordinates);
//
//        Query<City> query = unitOfWork.newQuery(
//                qb
//                        .where(                        ST_Within(cityTemplate.location(), builder.newInstance(), new Double(100.0))
//                                .or(
//                                        not(
//                                                ST_Within(cityTemplate.location(), builder.newInstance(), new Double(100.0)
//                                                )
//                                        )
//                                )));
//
//        System.out.println( "*** script01: " + query );
//        query.find();
//
//        System.out.println(query.count());
//
//
////        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
////        Person personTemplate = templateFor( Person.class );
////        City placeOfBirth = personTemplate.placeOfBirth().get();
////        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
////        System.out.println( "*** script04: " + query );
////       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }


    // @Test
    public void script3()
    {
//        Query<Person> query = unitOfWork.newQuery( module.newQueryBuilder( Person.class ).
//                where( eq( templateFor( Person.class ).money(),
//                        Money.of( CurrencyUnit.USD, 100 ) ) ) );
//
//        verifyUnorderedResults( query, "Joe Doe" );


        QueryBuilder<City> qb = this.module.newQueryBuilder( City.class );
        City cityTemplate = templateFor( City.class );
        Query<City> query = unitOfWork.newQuery( qb.where( eq( cityTemplate.name(), "Kuala Lumpur" ) ) );


        System.out.println( "*** script02: " + query );
        query.find();

        System.out.println(query.count());

//         System.out.println(query.find().locationABC());


//        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
//        Person personTemplate = templateFor( Person.class );
//        City placeOfBirth = personTemplate.placeOfBirth().get();
//        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
//        System.out.println( "*** script04: " + query );
//       //  verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    //@Test
    public void script4() throws Exception
    {

        System.out.println("Script4");

        QueryBuilder<City> qb = this.module.newQueryBuilder(City.class);
        // City cityTemplate = templateFor(City.class);

//        Query<City> query = unitOfWork.newQuery(
//                qb
//                        .where(
//                                ST_Within(
//                                        templateFor(City.class).location(),
//                                        ST_PointFromText(" POINT(-71.064544 42.28787)", 0))
//
//                        ));

 //       query.find();

    }
}

