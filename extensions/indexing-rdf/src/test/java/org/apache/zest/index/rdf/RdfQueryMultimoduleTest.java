package org.apache.zest.index.rdf;

import java.io.File;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.zest.library.rdf.repository.NativeConfiguration;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.util.DelTreeAfter;
import org.junit.Rule;

/**
 * User: ksr
 * Date: 27-09-2015
 * Time: 23:32
 */
public class RdfQueryMultimoduleTest
    extends RdfQueryTest
{
    private static final File DATA_DIR = new File( "build/tmp/rdf-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        LayerAssembly layer = module.layer();
        assembleValues( module, Visibility.module );
        assembleEntities( module, Visibility.module );

        ModuleAssembly storeModule = layer.module( "store" );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( storeModule );
        assembleValues( storeModule, Visibility.module );

        ModuleAssembly indexModule = layer.module( "index" );
        new RdfNativeSesameStoreAssembler( Visibility.layer, Visibility.module ).assemble( indexModule );

        LayerAssembly configLayer = module.layer().application().layer( "config" );
        module.layer().uses( configLayer );
        ModuleAssembly config = configLayer.module( "config" );
        config.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        config.forMixin( NativeConfiguration.class ).declareDefaults().dataDirectory().set( DATA_DIR.getAbsolutePath() );
        new EntityTestAssembler().assemble( config );
    }

}
