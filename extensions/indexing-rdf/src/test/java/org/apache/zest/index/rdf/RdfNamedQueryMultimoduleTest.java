package org.apache.zest.index.rdf;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.test.EntityTestAssembler;

/**
 * User: ksr
 * Date: 28-09-2015
 * Time: 10:28
 */
public class RdfNamedQueryMultimoduleTest
    extends RdfNamedQueryTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        LayerAssembly layer = module.layer();
        assembleEntities( module, Visibility.module );
        assembleValues( module, Visibility.module );

        ModuleAssembly storeModule = layer.module( "store" );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( storeModule );
        assembleValues( storeModule, Visibility.module );

        ModuleAssembly indexModule = layer.module( "index" );
        new RdfMemoryStoreAssembler( Visibility.layer, Visibility.module ).assemble( indexModule );
    }

}
