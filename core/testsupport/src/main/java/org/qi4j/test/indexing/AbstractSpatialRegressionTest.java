package org.qi4j.test.indexing;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.ExpressionSpecification;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.api.geometry.TGeometryFactory.TLinearRing;
import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Disjoint;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.junit.Assume.*;

/**
 * Created by jj on 21.12.14.
 */
public abstract class AbstractSpatialRegressionTest
        extends AbstractAnyQueryTest
{

    protected abstract boolean isExpressionSupported(Query<?> expression);

    public interface SpatialRegressionEntity extends EntityComposite, SpatialRegressionsValues {}

    public interface SpatialRegressionsValues
    {
        @Optional Property<TPoint>              point();
        @Optional Property<TMultiPoint>         multipoint();
        @Optional Property<TLineString>         line();
        @Optional Property<TPolygon>            polygon();
        @Optional Property<TMultiPolygon>       multipolygon();
        @Optional Property<TFeature>            feature();
        @Optional Property<TFeatureCollection>  featurecollection();
    }

    private TPoint      _TPoint,_TPoint2, _TPoint3;
    private TMultiPoint _TMultiPoint;
    private TLineString _TLineString;
    private TPolygon    _TPolygon;


    public void setUp() throws Exception {
        super.setUp();

        System.out.println("########### Populating Values ############");

        _TPoint = TPoint(module).lat(48.13905780942574).lon(11.57958984375)
                .geometry();

        _TPoint2 = TPoint(module).lat(48.145748).lon(11.567976)
                .geometry();

        _TPoint3 = TPoint(module).lat(52.518571).lon(13.404586)
                .geometry();

        _TMultiPoint = TMultiPoint(module).points(new double[][]
                {
                                                { 48.13905780942574 , 11.579589843750000 },
                                                { 48.14913756559802 , 11.599502563476562 }
                })
                .geometry();
        _TLineString = TlineString(module).points(new double[][]
                {
                                                { 48.109035906197036 , 11.550750732421875 },
                                                { 48.16608541901253  , 11.552810668945312 }
                })
                .geometry();
        _TPolygon = TPolygon(module)
                .shell
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                { 48.14478518644042 , 11.475906372070312 },
                                                { 48.18760570101003 , 11.572723388671875 },
                                                { 48.14043243818813 , 11.692886352539062 },
                                                { 48.08243697630599 , 11.679153442382812 },
                                                { 48.07211472138644 , 11.581306457519531 },
                                                { 48.10124109364004 , 11.522941589355469 },
                                                { 48.10949438777014 , 11.470069885253906 },
                                                { 48.14478518644042 , 11.475906372070312 }

                                        })
                                        .geometry()
                        )
                .withHoles
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                { 48.13837048124154 , 11.538391113281250 },
                                                { 48.15028286718964 , 11.614952087402344 },
                                                { 48.10513864768105 , 11.640357971191406 },
                                                { 48.10330454141599 , 11.558303833007812 },
                                                { 48.13837048124154 , 11.538391113281250 }

                                        })
                                        .geometry()
                        )
                .geometry();



        try (UnitOfWork unitOfWork = module.newUnitOfWork())
        {

            // TPoint
            {
                EntityBuilder<SpatialRegressionEntity> pointBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "Point");
                pointBuilder.instance().point().set(_TPoint);
                pointBuilder.newInstance();

                EntityBuilder<SpatialRegressionEntity> pointBuilder2 = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "Point2");
                pointBuilder2.instance().point().set(_TPoint2);
                pointBuilder2.newInstance();

                EntityBuilder<SpatialRegressionEntity> pointBuilder3 = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "Point3");
                pointBuilder3.instance().point().set(_TPoint3);
                pointBuilder3.newInstance();

            }

            // TMultiPoint
            {
                 EntityBuilder<SpatialRegressionEntity> mPointBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "MultiPoint");
                 mPointBuilder.instance().multipoint().set(_TMultiPoint);
                 mPointBuilder.newInstance();
            }

            // TLineString
            {
                 EntityBuilder<SpatialRegressionEntity> tlineStringBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "LineString");
                 tlineStringBuilder.instance().line().set(_TLineString);
                 tlineStringBuilder.newInstance();
            }

            // TPolygon
            {
                EntityBuilder<SpatialRegressionEntity> tPolygonBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "Polygon");
                tPolygonBuilder.instance().polygon().set(_TPolygon);
                tPolygonBuilder.newInstance();
            }

            unitOfWork.complete();
        }
    }




    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        module.entities(SpatialRegressionEntity.class);
    }


    // ST_Within()

    @Test
    public void script01()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
                                         TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                         10,TUnit.METER
                                        )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);
        // assertSame
    }

    @Test
    public void script02()
    {

        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
                                         TPoint(module).y(2389280.7514562616).x(1286436.5975464052).geometry("EPSG:27572"),
                                         10,TUnit.METER
                                        )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);

        // Transform(module).from(tPoint).to("EPSG:4326");
    }

    @Test
    public void script03()
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
                                         TPoint(module).y(48.13905780941111).x(11.57958981111).geometry(),
                                         10, TUnit.METER
                                        )
                        ))
                .orderBy(templateFor(SpatialRegressionsValues.class).point(), _TPoint, OrderBy.Order.ASCENDING);

        assumeTrue(isExpressionSupported(query));
        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);
    }

    @Ignore
    @Test
    public void script04()
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
                                         ST_GeometryFromText("POINT(11.57958981111 48.13905780941111 )"),
                                         10,TUnit.METER
                                        )
                        ))
                .orderBy(templateFor(SpatialRegressionsValues.class).point(), _TPoint, OrderBy.Order.ASCENDING);

        // assumeTrue(isExpressionSupported(query));
        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);
    }

    @Test
    public void script05()
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
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

        assumeTrue(isExpressionSupported(query));
        query.find();

        assertEquals(1, query.count());
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);
    }

    @Test
    public void script06()
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                        templateFor(SpatialRegressionsValues.class).line(),
                                        TPolygon(module)
                                         .shell
                                                                (
                                                                        new double[][]
                                                                                {{ 48.17341248658083 , 11.499938964843750  },
                                                                                        { 48.21003212234042 , 11.622848510742188  },
                                                                                        { 48.13470457551313 , 11.732711791992188  },
                                                                                        { 48.07280293614395 , 11.699409484863281  },
                                                                                        { 48.07372054150283 , 11.534614562988281  },
                                                                                        { 48.08817066753472 , 11.481056213378906  },
                                                                                        { 48.17341248658083 , 11.499938964843750  }}

                                                                ).geometry()
                                        )
                        ));

        // .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);

        assumeTrue(isExpressionSupported(query));

        query.find();
        assertEquals(1, query.count());
        TLineString tLineString = query.iterator().next().line().get();
        System.out.println(tLineString);
    }



    @Test
    public void script07()
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                        templateFor(SpatialRegressionsValues.class).polygon(),

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

    // ST_Disjoint()

    @Test
    public void script08()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoint
                                        (
                                                templateFor(SpatialRegressionsValues.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10,TUnit.METER
                                        )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();
        assertEquals(4, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);
        // assertSame
    }

    @Test
    public void script09()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoint
                                        (
                                                templateFor(SpatialRegressionsValues.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                // TPoint(module).y(45.13905780942574).x(10.57958984375).geometry(),
                                                10,TUnit.KILOMETER
                                        )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();

        System.out.println("Count " + query.count());

        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint3) == 0);
        // assertSame
    }


    @Test
    public void script10()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                ST_Disjoint
                                        (
                                                templateFor(SpatialRegressionsValues.class).point(),
                                                TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                10, TUnit.METER
                                        ),
                                ST_Within
                                        (
                                                 templateFor(SpatialRegressionsValues.class).point(),
                                                 TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                 10,TUnit.KILOMETER
                                         )
                                )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();

        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint2) == 0);
    }

    @Test
    public void script11()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                        ST_Disjoint
                                                (
                                                        templateFor(SpatialRegressionsValues.class).point(),
                                                        TPoint(module).y(2389280.7514562616).x(1286436.5975464052).geometry("EPSG:27572"),
                                                        10, TUnit.METER
                                                ),
                                        ST_Within
                                                (
                                                        templateFor(SpatialRegressionsValues.class).point(),
                                                        TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                                        100,TUnit.KILOMETER
                                                )
                                )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();

        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint2) == 0);
        // assertSame
    }

    @Test
    public void script12()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(and(
                                        ST_Disjoint
                                                (
                                                        templateFor(SpatialRegressionsValues.class).point(),
                                                        TPoint(module).y(2389280.7514562616).x(1286436.5975464052).geometry("EPSG:27572"),
                                                        10, TUnit.METER
                                                ),
                                        ST_Within
                                                (
                                                        templateFor(SpatialRegressionsValues.class).point(),
                                                        TPoint(module).y(2389280.7514562616).x(1286436.5975464052).geometry("EPSG:27572"),
                                                        1000,TUnit.KILOMETER
                                                )
                                )
                        ));
        assumeTrue(isExpressionSupported(query));
        query.find();

        assertEquals(2, query.count());
    }


}



