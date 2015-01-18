package org.qi4j.test.indexing;

import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.geometry.*;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.api.geometry.TGeometryFactory.TLinearRing;
import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.*;

/**
 * Created by jj on 21.12.14.
 */
public abstract class AbstractSpatialRegressionTest
        extends AbstractAnyQueryTest
{

    public interface A
    {
        @Optional Property<TPoint>              point();
        @Optional Property<TMultiPoint>         multipoint();
        @Optional Property<TLineString>         line();
        @Optional Property<TMultiLineString>    multiline();
        @Optional Property<TPolygon>            polygon();
        @Optional Property<TMultiPolygon>       multipolygon();
        @Optional Property<TFeature>            feature();
        @Optional Property<TFeatureCollection>  featurecollection();
        @Optional Association<Nested>           nested();
    }

    public interface Nested
    {
        @Optional Property<TPoint>              point();
    }

    public interface SpatialAEntity extends EntityComposite, A {}
    public interface SpatialBEntity extends EntityComposite, Nested {}


    private TPoint _TPoint1, _TPointNested,_TPoint2, _TPoint3;
    private TMultiPoint _TMultiPoint;
    private TLineString _TLineString;
    private TMultiLineString _TMultiLineString;
    private TMultiPolygon _TMultiPolygon;
    private TPolygon    _TPolygon;
    private TFeature    _TFeature;
    private TFeatureCollection _TFeatureCollection;


    public void setUp() throws Exception {
        super.setUp();

        System.out.println("########### Populating Values ############");

        _TPoint1 = TPoint(module).lat(48.13905780942574).lon(11.57958984375)
                .geometry();

        _TPointNested = TPoint(module).lat(48.13905780942574).lon(11.57958984375)
                .geometry();

        _TPoint2 = TPoint(module).lat(48.145748).lon(11.567976)
                .geometry();

        _TPoint3 = TPoint(module).lat(52.518571).lon(13.404586)
                .geometry();

        _TMultiPoint = TMultiPoint(module).points(new double[][]
                {
                        {48.13905780942574, 11.579589843750000},
                        {48.14913756559802, 11.599502563476562}
                })
                .geometry();

        _TLineString = TLineString(module).points(new double[][]
                {
                                                { 48.109035906197036 , 11.550750732421875 },
                                                { 48.16608541901253  , 11.552810668945312 }
                })
                .geometry();

        _TMultiLineString = TMultiLineString(module).of(_TLineString).of(_TLineString, _TLineString).geometry();

        _TPolygon = TPolygon(module)
                .shell
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {48.14478518644042, 11.475906372070312},
                                                {48.18760570101003, 11.572723388671875},
                                                {48.14043243818813, 11.692886352539062},
                                                {48.08243697630599, 11.679153442382812},
                                                {48.07211472138644, 11.581306457519531},
                                                {48.10124109364004, 11.522941589355469},
                                                {48.10949438777014, 11.470069885253906},
                                                {48.14478518644042, 11.475906372070312}

                                        })
                                        .geometry()
                        )
                .withHoles
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {48.13837048124154, 11.538391113281250},
                                                {48.15028286718964, 11.614952087402344},
                                                {48.10513864768105, 11.640357971191406},
                                                {48.10330454141599, 11.558303833007812},
                                                {48.13837048124154, 11.538391113281250}

                                        })
                                        .geometry()
                        )
                .geometry();

        _TMultiPolygon = TMultiPolygon(module).of(_TPolygon).of(_TPolygon, _TPolygon).geometry();

        _TFeature = TFeature(module).of(_TPoint1).addProperty("property", "feature").geometry();
        _TFeatureCollection = TFeatureCollection(module)
                                .of(TFeature(module).of(_TPoint1).addProperty("property", "point").geometry())
                                .of(TFeature(module).of(_TMultiPoint).addProperty("property", "multipoint").geometry())
                                .of(TFeature(module).of(_TLineString).addProperty("property", "linestring").geometry())
                                .of(TFeature(module).of(_TMultiLineString).addProperty("property", "multilinestring").geometry())
                                .of(TFeature(module).of(_TPolygon).addProperty("property", "polygon").geometry())
                                .of(TFeature(module).of(_TMultiPolygon).addProperty("property", "multipolygon").geometry())
                                .geometry();


        try (UnitOfWork unitOfWork = module.newUnitOfWork())
        {

            // TPoint
            {
                EntityBuilder<SpatialBEntity> pointBuilderNested = unitOfWork.newEntityBuilder(SpatialBEntity.class, "Nested");
                pointBuilderNested.instance().point().set(_TPointNested);
                SpatialBEntity nested = pointBuilderNested.newInstance();

                EntityBuilder<SpatialAEntity> pointBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "Point1");
                pointBuilder.instance().point().set(_TPoint1);
                pointBuilder.instance().nested().set(nested);
                pointBuilder.newInstance();

                EntityBuilder<SpatialAEntity> pointBuilder2 = unitOfWork.newEntityBuilder(SpatialAEntity.class, "Point2");
                pointBuilder2.instance().point().set(_TPoint2);
                pointBuilder2.newInstance();

                EntityBuilder<SpatialAEntity> pointBuilder3 = unitOfWork.newEntityBuilder(SpatialAEntity.class, "Point3");
                pointBuilder3.instance().point().set(_TPoint3);
                pointBuilder3.newInstance();
            }

            // TMultiPoint
            {
                 EntityBuilder<SpatialAEntity> mPointBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "MultiPoint");
                 mPointBuilder.instance().multipoint().set(_TMultiPoint);
                 mPointBuilder.newInstance();
            }

            // TLineString
            {
                 EntityBuilder<SpatialAEntity> tlineStringBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "LineString");
                 tlineStringBuilder.instance().line().set(_TLineString);
                 tlineStringBuilder.newInstance();
            }
            // TMultiLineString
            {
                EntityBuilder<SpatialAEntity> tlineStringBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "MultiLineString");
                tlineStringBuilder.instance().multiline().set(_TMultiLineString);
                tlineStringBuilder.newInstance();
            }

            // TPolygon
            {
                EntityBuilder<SpatialAEntity> tPolygonBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "Polygon");
                tPolygonBuilder.instance().polygon().set(_TPolygon);
                tPolygonBuilder.newInstance();
            }

            // TMultiPolygon
            {
                EntityBuilder<SpatialAEntity> tPolygonBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "MultiPolygon");
                tPolygonBuilder.instance().multipolygon().set(_TMultiPolygon);
                tPolygonBuilder.newInstance();
            }

            // TFeature
            {
                EntityBuilder<SpatialAEntity> tFeatureBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "Feature");
                tFeatureBuilder.instance().feature().set(_TFeature);
                tFeatureBuilder.newInstance();
            }

            // TFeatureCollection
            {
                EntityBuilder<SpatialAEntity> tFeatureCollectionBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class, "FeatureCollection");
                tFeatureCollectionBuilder.instance().featurecollection().set(_TFeatureCollection);
                tFeatureCollectionBuilder.newInstance();
            }
            unitOfWork.complete();
        }
    }




    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        module.entities(SpatialAEntity.class, SpatialBEntity.class);
    }



    @Test
    public void script01a()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(A.class).point(),
                                         TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                         10,TUnit.METER
                                        )
                        ));
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint1) == 0);
    }

    @Test
    public void script01b()
            throws EntityFinderException
    {

        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(1286436.5975464052).x(2389280.7514562616).geometry("EPSG:27572"),
                                                10, TUnit.METER
                                        )
                        ));
        System.out.println(query);
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint1) == 0);
    }

    @Test
    public void script01c() throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(A.class).point(),
                                         TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                         10, TUnit.METER
                                        )
                        ))
                .orderBy(templateFor(A.class).point(), _TPoint1, OrderBy.Order.ASCENDING);

        System.out.println(query);
        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint1) == 0);
    }

    @Test
    public void script01d()  throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                ST_Within
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                                10, TUnit.METER
                                        )
                                        ,
                                ST_Within
                                         (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                                 5, TUnit.METER
                                                )
                                ))
                        )
                .orderBy(templateFor(A.class).point(), _TPoint1, OrderBy.Order.ASCENDING);
        System.out.println(query);
        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint1) == 0);
    }

    @Test
    public void script01e()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                ST_Within
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                                1000, TUnit.KILOMETER
                                        )
                                ,
                                not(ST_Within
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                                1, TUnit.METER
                                        ))
                        ))
        )
        .orderBy(templateFor(A.class).point(), _TPoint1, OrderBy.Order.ASCENDING);

        query.find();
        assertEquals(query.count(), 2);

        Iterator<A> results = query.iterator();

        // sorted ascending by distance
        TPoint tPoint2 = results.next().point().get();
        TPoint tPoint3 = results.next().point().get();

        assertTrue(tPoint2.compareTo(_TPoint2) == 0);
        assertTrue(tPoint3.compareTo(_TPoint3) == 0);
    }

    @Test
    public void script01f()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                ST_Within
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                                1000, TUnit.KILOMETER
                                        )
                                ,
                                not(ST_Within
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                                1, TUnit.METER
                                        ))
                        ))
        )
                .orderBy(templateFor(A.class).point(), _TPoint1, OrderBy.Order.DESCENDING);

        query.find();
        assertEquals(query.count(), 2);

        Iterator<A> results = query.iterator();

        // sorted descending by distance
        TPoint tPoint3 = results.next().point().get();
        TPoint tPoint2 = results.next().point().get();

        assertTrue(tPoint2.compareTo(_TPoint2) == 0);
        assertTrue(tPoint3.compareTo(_TPoint3) == 0);
    }


    @Ignore // <-- WKT support disabled
    @Test
    public void script01g()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(A.class).point(),
                                         ST_GeometryFromText("POINT(11.57958981111 48.13905780941111 )"),
                                         10,TUnit.METER
                                        )
                        ))
                .orderBy(templateFor(A.class).point(), _TPoint1, OrderBy.Order.ASCENDING);

        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint1) == 0);
    }

    @Test
    public void script01h()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(A.class).point(),
                                         TPolygon(module)
                                         .shell
                                                 (
                                                 new double[][]
                                                         {
                                                            { 48.14255158541622 , 11.575984954833984 },
                                                            { 48.14177839318570 , 11.586370468139648 },
                                                            { 48.13416039532121 , 11.587958335876465 },
                                                            { 48.12994996417526 , 11.584053039550781 },
                                                            { 48.12900471789726 , 11.570577621459961 },
                                                            { 48.13519146869094 , 11.563453674316406 },
                                                            { 48.14063290180734 , 11.565470695495605 },
                                                            { 48.14189294091770 , 11.570920944213867 },
                                                            { 48.14255158541622 , 11.575984954833984 }
                                                         }
                                                  )
                                                 .geometry()
                                        )
                        ));

        query.find();

        assertEquals(1, query.count());
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint1) == 0);
    }

    @Test
    public void script01i()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                        templateFor(A.class).line(),
                                        TPolygon(module)
                                         .shell
                                                 (
                                                         new double[][]

                                                                 {
                                                                         {48.17341248658083, 11.499938964843750},
                                                                         {48.21003212234042, 11.622848510742188},
                                                                         {48.13470457551313, 11.732711791992188},
                                                                         {48.07280293614395, 11.699409484863281},
                                                                         {48.07372054150283, 11.534614562988281},
                                                                         {48.08817066753472, 11.481056213378906},
                                                                         {48.17341248658083, 11.499938964843750}
                                                                 }

                                                 ).geometry()
                                        )
                        ));
        query.find();
        assertEquals(1, query.count());
        TLineString tLineString = query.iterator().next().line().get();
    }



    @Test
    public void script01j()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                        templateFor(A.class).polygon(),

                                         TPolygon(module)
                                              .shell
                                                   (
                                                   new double[][]
                                                          {
                                                                          { 48.122101028190805 , 11.329650878906250 },
                                                                          { 48.285934388727240 , 11.394195556640625 },
                                                                          { 48.232906106325146 , 11.936645507812500 },
                                                                          { 47.950385640510110 , 11.852874755859375 },
                                                                          { 47.944866579210150 , 11.368103027343750 },
                                                                          { 48.122101028190805 , 11.329650878906250 }
                                                          }
                                                                ).geometry()
                                        )
                        ));


        query.find();

        assertEquals(query.count(), 1);
        TPolygon tPolygon = query.iterator().next().polygon().get();
        assertTrue(tPolygon.holes().get().size() == 1);
        assertTrue(tPolygon.shell().get().compareTo(_TPolygon.shell().get()) == 0);
        assertFalse(tPolygon.holes().get().get(0).compareTo(_TPolygon.shell().get()) == 0);
        assertTrue(tPolygon.holes().get().get(0).compareTo(_TPolygon.holes().get().get(0)) == 0);
    }

    @Test
    public void script01k()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(A.class).nested().get().point(), // <- "nested.point"
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10,TUnit.METER
                                        )
                        ));
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPointNested) == 0);
    }

    // ST_Disjoint()

    @Test
    public void script02a()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoint
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10,TUnit.METER
                                        )
                        ));
        query.find();
        assertEquals(2, query.count());
    }

    @Test
    public void script02b()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoint
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10,TUnit.KILOMETER
                                        )
                        ));
        query.find();

        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint3) == 0);
    }


    @Test
    public void script02c()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                ST_Disjoint
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10, TUnit.METER
                                        ),
                                ST_Within
                                        (
                                                 templateFor(A.class).point(),
                                                 TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                 10,TUnit.KILOMETER
                                         )
                                )
                        ));
        query.find();

        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint2) == 0);
    }

    @Test
    public void script02d()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                        ST_Disjoint
                                                (
                                                        templateFor(A.class).point(),
                                                        TPoint(module).y(2389280.7514562616).x(1286436.5975464052).geometry("EPSG:27572"),
                                                        10, TUnit.METER
                                                ),
                                        ST_Within
                                                (
                                                        templateFor(A.class).point(),
                                                        TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                        100,TUnit.KILOMETER
                                                )
                                )
                        ));
        query.find();

        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint2) == 0);
        // assertSame
    }

    @Test
    public void script02e()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                        ST_Disjoint
                                                (
                                                        templateFor(A.class).point(),
                                                        TPoint(module).y(1286436.5975464052).x(2389280.7514562616).geometry("EPSG:27572"),
                                                        10, TUnit.METER
                                                ),
                                        ST_Within
                                                (
                                                        templateFor(A.class).point(),
                                                        TPoint(module).y(1286436.5975464052).x(2389280.7514562616).geometry("EPSG:27572"),
                                                        1000,TUnit.KILOMETER
                                                )
                                )
                        ));
        query.find();

        assertEquals(2, query.count());
    }


    @Test
    public void script02f()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoint
                                        (
                                                templateFor(A.class).nested().get().point(),
                                                TPoint(module).y(49.13905780942574).x(12.57958984375).geometry(),
                                                10,TUnit.METER
                                        )
                        ));
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPointNested) == 0);
    }

    // ST_Intersects()

    @Test
    public void script03a()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Intersects
                                        (
                                                templateFor(A.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10,TUnit.METER
                                        )
                        ));
        query.find();
        assertEquals(1, query.count());
    }

    @Test
    public void script03b()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Intersects
                                        (
                                                templateFor(A.class).polygon(),

                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        { 48.160131, 11.778717 },
                                                                                        { 48.156925, 11.631775 },
                                                                                        { 48.061561, 11.600876 },
                                                                                        { 48.006922, 11.778030 },
                                                                                        { 48.062020, 11.858368 },
                                                                                        { 48.159215, 11.778717 }
                                                                                }
                                                                ).geometry()
                                        )
                        ));


        query.find();

        assertEquals(query.count(), 1);
        TPolygon tPolygon = query.iterator().next().polygon().get();
        assertTrue(tPolygon.holes().get().size() == 1);
        assertTrue(tPolygon.shell().get().compareTo(_TPolygon.shell().get()) == 0);
        assertFalse(tPolygon.holes().get().get(0).compareTo(_TPolygon.shell().get()) == 0);
        assertTrue(tPolygon.holes().get().get(0).compareTo(_TPolygon.holes().get().get(0)) == 0);
    }

    @Test
    public void script03c()
            throws EntityFinderException
    {
        QueryBuilder<A> qb = this.module.newQueryBuilder(A.class);

        Query<A> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Intersects
                                        (
                                                templateFor(A.class).polygon(),

                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        { 48.160131, 11.778717 },
                                                                                        { 48.156925, 11.631775 },
                                                                                        { 48.061561, 11.600876 },
                                                                                        { 48.006922, 11.778030 },
                                                                                        { 48.062020, 11.858368 },
                                                                                        { 48.159215, 11.778717 }
                                                                                }
                                                                ).geometry()
                                        )
                        ))
                .orderBy(templateFor(A.class).point(), _TPoint1, OrderBy.Order.ASCENDING);


        query.find();

        assertEquals(query.count(), 1);
        TPolygon tPolygon = query.iterator().next().polygon().get();
        assertTrue(tPolygon.holes().get().size() == 1);
        assertTrue(tPolygon.shell().get().compareTo(_TPolygon.shell().get()) == 0);
        assertFalse(tPolygon.holes().get().get(0).compareTo(_TPolygon.shell().get()) == 0);
        assertTrue(tPolygon.holes().get().get(0).compareTo(_TPolygon.holes().get().get(0)) == 0);
    }

}



