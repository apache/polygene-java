package org.qi4j.index.elasticsearch.extension.spatial;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.query.Query;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.spatial.v2.assembly.TGeometryAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialRegressionTest;
import org.qi4j.test.util.DelTreeAfter;

import java.io.File;

import static org.qi4j.test.util.Assume.*;

/**
 * Created by jj on 21.12.14.
 */
public class ElasticSearchSpatialRegressionQueryVariant2Test
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
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        config.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
                withConfig(config,Visibility.layer ).
                identifiedBy("ElasticSearchConfigurationVariant2").
                assemble(module);

        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );
        esConfig.indexPointMappingMethod().set(ElasticSearchConfiguration.INDEX_MAPPING_POINT_METHOD.GEO_POINT);


        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
                withData( new File( DATA_DIR, "qi4j-data" ) ).
                withLog( new File( DATA_DIR, "qi4j-logs" ) ).
                withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
                setMetaInfo( override );


        config.services(FileConfigurationService.class)
                // .identifiedBy("ElasticSearchConfigurationVariant1")
                .setMetaInfo(override)
                .visibleIn(Visibility.application);

        // clear index mapping caches during junit testcases
        // SpatialIndexMapper.IndexMappingCache.clear();
    }
}
