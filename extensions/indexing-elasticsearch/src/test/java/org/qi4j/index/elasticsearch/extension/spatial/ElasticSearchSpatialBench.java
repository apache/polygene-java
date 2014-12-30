package org.qi4j.index.elasticsearch.extension.spatial;

import com.spatial4j.core.distance.DistanceUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.assembly.DerbySQLEntityStoreAssembler;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extension.spatial.utils.RandomPoint;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.spatial.v2.assembly.TGeometryAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialRegressionTest;
import org.qi4j.test.util.DelTreeAfter;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import java.io.File;

import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.test.util.Assume.*;

/**
 * Created by jj on 21.12.14.
 */
public class ElasticSearchSpatialBench
        extends AbstractSpatialRegressionTest
{
    private static final File DATA_DIR = new File( "build/tmp/es-spatial-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    protected boolean isExpressionSupported(Query<?> expression)
    {
        return true;
    }

/**
    protected boolean isExpressionSupported(Query<?> expression)
    {
        QueryImpl queryImpl = (QueryImpl)expression;
        System.out.println("### " + expression.getClass());

        System.out.println(queryImpl.resultType());

        System.out.println("getWhereClause " + queryImpl.getWhereClause().getClass().getSimpleName());

        System.out.println(((SpatialPredicatesSpecification)queryImpl.getWhereClause()).value());

        boolean hasOrderBySegments = false;
        if (queryImpl.getOrderBySegments() != null && queryImpl.getOrderBySegments().iterator().hasNext())
        {
            hasOrderBySegments = true;
        }
        // public static boolean isSupported(Class expression, Class<? extends  TGeometry> geometryOfProperty,Class<? extends  TGeometry> geometryOfFilter, Boolean orderBy, INDEXING_METHOD Type )

        Class geometryOfProperty = InternalUtils.classOfPropertyType(((SpatialPredicatesSpecification)queryImpl.getWhereClause()).property());
        TGeometry geometryOfFilter   = ((SpatialPredicatesSpecification)queryImpl.getWhereClause()).value();

        // System.out.println("Operator " + ((SpatialPredicatesSpecification)queryImpl.getWhereClause()).operator().getClass());

        System.out.println("geometryOfProperty " + geometryOfProperty);
        System.out.println("geometryOfFilter   " + InternalUtils.classOfGeometry(geometryOfFilter));

        System.out.println("Exression " + expression.getClass());

        return SpatialFunctionsSupportMatrix.isSupported
                (
                        queryImpl.getWhereClause().getClass(),
                        geometryOfProperty,
                        InternalUtils.classOfGeometry(geometryOfFilter),
                        hasOrderBySegments,
                        SpatialFunctionsSupportMatrix.INDEX_MAPPING_TPOINT_METHOD.TPOINT_AS_GEOPOINT
                );
    }
 */

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        // Geometry support
        new TGeometryAssembler().assemble(module);



        // Config module
        ModuleAssembly configIndex = module.layer().module( "configIndex" );
        new EntityTestAssembler().assemble( configIndex );

        configIndex.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
                withConfig(configIndex,Visibility.layer ).
                identifiedBy("ElasticSearchConfigurationVariant2").
                assemble(module);

        ElasticSearchConfiguration esConfig = configIndex.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );
        esConfig.indexPointMappingMethod().set(ElasticSearchConfiguration.INDEX_MAPPING_POINT_METHOD.GEO_POINT);


        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
                withData( new File( DATA_DIR, "qi4j-data" ) ).
                withLog( new File( DATA_DIR, "qi4j-logs" ) ).
                withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
                setMetaInfo( override );


        configIndex.services(FileConfigurationService.class)
                // .identifiedBy("ElasticSearchConfigurationVariant1")
                .setMetaInfo(override)
                .visibleIn(Visibility.application);

        // clear index mapping caches during junit testcases
        // SpatialIndexMapper.IndexMappingCache.clear();

        ModuleAssembly configStore = module.layer().module( "configStore" );
        new EntityTestAssembler().assemble( configStore );
        new OrgJsonValueSerializationAssembler().assemble( module );

        // START SNIPPET: assembly
        // DataSourceService
        new DBCPDataSourceServiceAssembler().
                identifiedBy( "derby-datasource-service" ).
                visibleIn( Visibility.module ).
                withConfig( configStore, Visibility.layer ).
                assemble( module );

        // DataSource
        new DataSourceAssembler().
                withDataSourceServiceIdentity( "derby-datasource-service" ).
                identifiedBy( "derby-datasource" ).
                visibleIn( Visibility.module ).
                withCircuitBreaker().
                assemble( module );

        // SQL EntityStore
        new DerbySQLEntityStoreAssembler().
                visibleIn( Visibility.application ).
                withConfig( configStore, Visibility.layer ).
                assemble( module );
    }

    @Test
    public void test() throws Exception
    {

        try (UnitOfWork unitOfWork = module.newUnitOfWork())
        {
            unitOfWork.complete();

        }
        // double[] xy = nextSpherePt2D();

        // System.out.println("spherical " + xy[0] + " " + xy[1] );
        long start = System.currentTimeMillis();

        module.newUnitOfWork();
        for (int i = 0; i < 10000; i++) {

            double[] xy = nextSpherePt2D();
            System.out.println("Degrees " + DistanceUtils.toDegrees(xy[0]) + "," + DistanceUtils.toDegrees(xy[1]));

            TPoint(module).lat(xy[0]).lon(xy[1]).geometry();
        }
        module.currentUnitOfWork().complete();

        long end = System.currentTimeMillis();

        System.out.println("Duration  " + (end - start));
    }

    @Test
    public void test1() throws Exception
    {

        try (UnitOfWork unitOfWork = module.newUnitOfWork())
        {
            unitOfWork.complete();

        }
        // double[] xy = nextSpherePt2D();

        // System.out.println("spherical " + xy[0] + " " + xy[1] );
        long start = System.currentTimeMillis();
        for (int j = 0; j < 1000; j++) {

            System.out.println("--> " + j);
            UnitOfWork unitOfWork = module.newUnitOfWork();


            for (int i = 0; i < 5000; i++) {
                double[] xy = nextSpherePt2D();
                //System.out.println("Degrees " + DistanceUtils.toDegrees(xy[0]) + "," + DistanceUtils.toDegrees(xy[1]));

                TPoint point = TPoint(module).lat(DistanceUtils.toDegrees(xy[0])).lon(DistanceUtils.toDegrees(xy[1])).geometry();
                EntityBuilder<SpatialRegressionEntity> pointBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class);
                pointBuilder.instance().point().set(point);
                pointBuilder.newInstance();
            }

            unitOfWork.complete();
        }
        long end = System.currentTimeMillis();



        System.out.println("Duration  " + (end - start));
    }

     static long seed = 1;
    static RandomPoint randomPoint = new RandomPoint();

    public double[] nextSpherePt2D()
    {
        // return new RandomPoint(seed++).nextSpherePt(2);
        return randomPoint.nextSpherePt(2);
    }
}
