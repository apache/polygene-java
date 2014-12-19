package org.qi4j.index.elasticsearch.extension.spatial;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TGeometryRoot;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESClusterIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extension.spatial.model.VerifyStatialTypes;
import org.qi4j.index.elasticsearch.extension.spatial.model.entity.SpatialEntity;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialQueryTest;
import org.qi4j.test.indexing.model.Domain;
import org.qi4j.test.indexing.model.Nameable;
import org.qi4j.test.util.DelTreeAfter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;

import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.*;
import static org.qi4j.test.indexing.NameableAssert.verifyUnorderedResults;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

/**
 * Created by jakes on 2/8/14.
 */
public class ElasticSearchSpatialExtensionClusterQueryTest
        extends AbstractSpatialQueryTest {
    private static final File DATA_DIR = new File("build/tmp/es-money-query-test");
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter(DATA_DIR);

    @BeforeClass
    public static void beforeClass_IBMJDK() {
        assumeNoIbmJdk();
    }

    // Types definition of later comparison
    private TPoint _tPoint;
    private TMultiPoint _tMultiPoint;
    private TLineString _tLineString;
    private TPolygon _tPolygon;


    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException {
        super.assemble(module);

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
    }

    // @Override
    public void setUp()
            throws Exception {
        super.setUp();

        _tPoint = TPoint(module).x(11.57958984375).y(48.13905780942574).geometry();

        _tMultiPoint = TMultiPoint(module).points(new double[][]
                {
                        {11.57958984375, 48.13905780942574},
                        {11.599502563476562, 48.14913756559802},
                }).geometry();
        _tLineString = TlineString(module).points(new double[][]
                {
                        {11.550750732421875, 48.109035906197036},
                        {11.552810668945312, 48.16608541901253},
                }).geometry();

        _tPolygon = TPolygon(module)

                .shell
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {11.475906372070312, 48.14478518644042},
                                                {11.572723388671875, 48.18760570101003},
                                                {11.692886352539062, 48.140432438188135},
                                                {11.679153442382812, 48.08243697630599},
                                                {11.581306457519531, 48.07211472138644},
                                                {11.522941589355469, 48.10124109364004},
                                                {11.470069885253906, 48.10949438777014},
                                                {11.475906372070312, 48.14478518644042},

                                        }).geometry()
                        )

                .withHoles
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                {11.53839111328125, 48.13837048124154},
                                                {11.614952087402344, 48.15028286718964},
                                                {11.640357971191406, 48.10513864768105},
                                                {11.558303833007812, 48.103304541415994},
                                                {11.53839111328125, 48.13837048124154}

                                        }).geometry()
                        )
                .geometry();


        try (UnitOfWork unitOfWork = module.newUnitOfWork()) {


            // TPoint
            {
                EntityBuilder<SpatialEntity> pointBuilder = unitOfWork.newEntityBuilder(SpatialEntity.class, "Point");
                pointBuilder.instance().point().set(_tPoint);
                pointBuilder.newInstance();
            }

            // TMultiPoint
            {
                EntityBuilder<SpatialEntity> mPointBuilder = unitOfWork.newEntityBuilder(SpatialEntity.class, "MultiPoint");
                mPointBuilder.instance().mPoint().set(_tMultiPoint);
                mPointBuilder.newInstance();
            }

            // TLineString
            {
                EntityBuilder<SpatialEntity> tlineStringBuilder = unitOfWork.newEntityBuilder(SpatialEntity.class, "LineString");
                tlineStringBuilder.instance().line().set(_tLineString);
                tlineStringBuilder.newInstance();
            }

            // TPolygon
            {
                EntityBuilder<SpatialEntity> tPolygonBuilder = unitOfWork.newEntityBuilder(SpatialEntity.class, "Polygon");
                tPolygonBuilder.instance().polygon().set(_tPolygon);
                tPolygonBuilder.newInstance();
            }

            unitOfWork.complete();

        } catch (Exception _ex) {
            _ex.printStackTrace();
        }

    }

    @Test
    public void nothing()
    {

    }


    @Test
    public void WhenQueryForAPointThenCompareResults()
    {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPoint(module).x(11.57958981111).y(48.13905780941111).geometry(),
                                                999,
                                                TUnit.METER
                                        )
                        ));


        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_tPoint) == 0);
    }


    @Test
    public void WhenQueryWithUnsupportedProjectionForAPointThenCompareResults() throws Exception {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPoint(module).x(1286436.5975464052).y(2389280.7514562616).geometry("EPSG:27572"),
                                                999,
                                                TUnit.METER
                                        )
                        ));


        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_tPoint) == 0);

        // Transform(module).from(tPoint).to("EPSG:27572");

        System.out.println(tPoint);
    }

    @Test
    public void WhenQueryForAPointThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPoint(module).x(11.57958981111).y(48.13905780941111).geometry(),
                                                999,
                                                TUnit.METER
                                        )
                        ))


                .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_tPoint) == 0);
    }


    @Test
    public void script02()
            throws EntityFinderException
    {
        final QueryBuilder<Domain> qb = this.module.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        final Query<Domain> query = unitOfWork.newQuery( qb.where( eq( nameable.name(), "Gaming" ) ) );
        System.out.println( "*** script02: " + query );
        verifyUnorderedResults( query, "Gaming" );
    }


    @Test
    public void WhenST_DisjointForAPointThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(// and(
                                ST_Disjoint
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPoint(module).x(10.57958981111).y(47.13905780941111).geometry(),
                                                1,
                                                TUnit.METER
                                        )
                                       //  eq( templateFor(VerifyStatialTypes.class).point().get(), "Gaming" )
                                        // ,QueryExpressions.eq(templateFor(VerifyStatialTypes.class).foo(), "foo")
                                // )
                        ))


                        .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(3, query.count());
        // TGeometry tGeometry = query.iterator().next().point().get();

        VerifyStatialTypes result = query.iterator().next();

/**
        if (tGeometry.getType() == TGeometry.TGEOMETRY_TYPE.POLYGON)
        {
            System.out.println("is polygon");
        }
 */
        // assertTrue(tPoint.compareTo(_tPoint) == 0);

    }


    @Test
    public void WhenST_IntersectsForAPointThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Intersects
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPoint(module).x(11.57958981111).y(48.13905780941111).geometry(),
                                                999,
                                                TUnit.METER
                                        )
                        ))


                .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(3, query.count());
        // TGeometry tGeometry = query.iterator().next().point().get();

        VerifyStatialTypes result = query.iterator().next();

/**
 if (tGeometry.getType() == TGeometry.TGEOMETRY_TYPE.POLYGON)
 {
 System.out.println("is polygon");
 }
 */
        // assertTrue(tPoint.compareTo(_tPoint) == 0);

    }

    @Test
    public void WhenST_DisjointForAPointUsingPolygonAreaThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Disjoint
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {11.32965087890625, 48.122101028190805},
                                                                                        {11.394195556640625, 48.28593438872724},
                                                                                        {11.9366455078125, 48.232906106325146},
                                                                                        {11.852874755859375, 47.95038564051011},
                                                                                        {11.36810302734375, 47.94486657921015},
                                                                                        {11.32965087890625, 48.122101028190805}
                                                                                }
                                                                ).geometry()
                                        )
                        ))

                .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        // assertEquals(query.count(), 1);
        // TPoint tPoint = query.iterator().next().point().get();
        VerifyStatialTypes types = query.iterator().next();
        System.out.println("Result " + types.polygon().get());
        // assertTrue(tPoint.compareTo(_tPoint) == 0);
    }


    @Test
    public void WhenQueryForAPointUsingPolygonAreaThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {11.32965087890625, 48.122101028190805},
                                                                                        {11.394195556640625, 48.28593438872724},
                                                                                        {11.9366455078125, 48.232906106325146},
                                                                                        {11.852874755859375, 47.95038564051011},
                                                                                        {11.36810302734375, 47.94486657921015},
                                                                                        {11.32965087890625, 48.122101028190805}
                                                                                }
                                                                ).geometry()
                                        )
                        ))

                .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_tPoint) == 0);
    }

    @Test
    public void WhenQueryForALineUsingPolygonAreaThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).line(),
                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {11.32965087890625, 48.122101028190805},
                                                                                        {11.394195556640625, 48.28593438872724},
                                                                                        {11.9366455078125, 48.232906106325146},
                                                                                        {11.852874755859375, 47.95038564051011},
                                                                                        {11.36810302734375, 47.94486657921015},
                                                                                        {11.32965087890625, 48.122101028190805}
                                                                                }
                                                                ).geometry()
                                        )
                        ));

                // .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(1, query.count());
        TLineString tLineString = query.iterator().next().line().get();
        System.out.println(tLineString);
        // TPoint tPoint = query.iterator().next().point().get();
        // assertTrue(tPoint.compareTo(_tPoint) == 0);
    }


    @Test
    public void WhenQueryForALineUsingPolygonAreaThenOrderAndCompareResultsV2() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {11.32965087890625, 48.122101028190805},
                                                                                        {11.394195556640625, 48.28593438872724},
                                                                                        {11.9366455078125, 48.232906106325146},
                                                                                        {11.852874755859375, 47.95038564051011},
                                                                                        {11.36810302734375, 47.94486657921015},
                                                                                        {11.32965087890625, 48.122101028190805}
                                                                                }
                                                                ).geometry()
                                        )
                        )).orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(1, query.count());
        TLineString tLineString = query.iterator().next().line().get();
        System.out.println(tLineString);
        // TPoint tPoint = query.iterator().next().point().get();
        // assertTrue(tPoint.compareTo(_tPoint) == 0);
    }


    @Test
    public void WhenST_Intersects_ForALineUsingPolygonAreaThenOrderAndCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Intersects
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {11.32965087890625, 48.122101028190805},
                                                                                        {11.394195556640625, 48.28593438872724},
                                                                                        {11.9366455078125, 48.232906106325146},
                                                                                        {11.852874755859375, 47.95038564051011},
                                                                                        {11.36810302734375, 47.94486657921015},
                                                                                        {11.32965087890625, 48.122101028190805}
                                                                                }
                                                                ).geometry()
                                        )
                        ));

        // .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);


        query.find();
        assertEquals(1, query.count());
        TLineString tLineString = query.iterator().next().line().get();
        System.out.println(tLineString);
        // TPoint tPoint = query.iterator().next().point().get();
        // assertTrue(tPoint.compareTo(_tPoint) == 0);
    }


    @Test
    public void When_QueryForAWKTPointThenCompareResults()
    {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).point(),
                                                ST_GeometryFromText("POINT(11.57958981111 48.13905780941111 )"),
                                                999,
                                                TUnit.METER
                                        )
                        ))
                .orderBy(templateFor(VerifyStatialTypes.class).point(), _tPoint, OrderBy.Order.ASCENDING);

        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_tPoint) == 0);

    }

    /**
    @Test
    public void WhenQueryForAMultiPointThenCompareResults() {

        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).mPoint(),
                                                TPOINT(module).x(11.57958981111).y(48.13905780941111).geometry()
                                        )
                        ));
        query.find();
        assertEquals(query.count(), 1);
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_tPoint) == 0);
    }
*/

        @Test
    public void WhenQueryForAPolygonThenCompareResults() {
        QueryBuilder<VerifyStatialTypes> qb = this.module.newQueryBuilder(VerifyStatialTypes.class);

        Query<VerifyStatialTypes> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                                templateFor(VerifyStatialTypes.class).polygon(),

                                                TPolygon(module)
                                                        .shell
                                                                (
                                                                        new double[][]
                                                                                {
                                                                                        {11.32965087890625, 48.122101028190805},
                                                                                        {11.394195556640625, 48.28593438872724},
                                                                                        {11.9366455078125, 48.232906106325146},
                                                                                        {11.852874755859375, 47.95038564051011},
                                                                                        {11.36810302734375, 47.94486657921015},
                                                                                        {11.32965087890625, 48.122101028190805}
                                                                                }
                                                                ).geometry()
                                        )
                        ));


        query.find();

        assertEquals(query.count(), 1);
        TPolygon tPolygon = query.iterator().next().polygon().get();
        assertTrue(tPolygon.holes().get().size() == 1);
        assertTrue(tPolygon.shell().get().compareTo(_tPolygon.shell().get()) == 0);
        assertFalse(tPolygon.holes().get().get(0).compareTo(_tPolygon.shell().get()) == 0);
        assertTrue(tPolygon.holes().get().get(0).compareTo(_tPolygon.holes().get().get(0)) == 0);
    }


    // @Test
    public void fooPerf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            WhenQueryForAPolygonThenCompareResults();
        }
        long end = System.currentTimeMillis();

        System.out.println("Duration " + (end - start) + " ms");
    }

}