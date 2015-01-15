package org.qi4j.index.elasticsearch.extension.spatial;

import com.spatial4j.core.distance.DistanceUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.riak.RiakHttpMapEntityStoreAssembler;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESClusterIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extension.spatial.utils.RandomPoint;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractAnyQueryTest;
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
        new ESClusterIndexQueryAssembler().
                withConfig(config, Visibility.layer).
                identifiedBy("ElasticSearchBenchmark").
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
        new RiakHttpMapEntityStoreAssembler().identifiedBy("RIAKBenchmark").withConfig(configRiak, Visibility.layer ).assemble( module );
        /** +++ */
    }

    // @Test
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
        for (int j = 0; j < 10000; j++)
        {
            System.out.println("--> " + j);
            UnitOfWork unitOfWork = module.newUnitOfWork();


            for (int i = 0; i < 1000; i++) {
                double[] xy = nextSpherePt2D();
                //System.out.println("Degrees " + DistanceUtils.toDegrees(xy[0]) + "," + DistanceUtils.toDegrees(xy[1]));

                TPoint point = TPoint(module).lat(DistanceUtils.toDegrees(xy[0])).lon(DistanceUtils.toDegrees(xy[1])).geometry();
                EntityBuilder<SpatialAEntity> pointBuilder = unitOfWork.newEntityBuilder(SpatialAEntity.class);
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
        return randomPoint.nextSpherePt(2);
    }
}
