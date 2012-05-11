package org.qi4j.library.sql.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.datasource.DataSourceService;

/**
 * Use this Assembler to register a DataSourceService and its Configuration entity.
 */
public class DataSourceServiceAssembler
        implements Assembler
{

    private String dataSourceServiceId;

    private ModuleAssembly configModuleAssembly;

    public DataSourceServiceAssembler( String dataSourceServiceId, ModuleAssembly configModuleAssembly )
    {
        this.dataSourceServiceId = dataSourceServiceId;
        this.configModuleAssembly = configModuleAssembly;
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( DataSourceService.class ).identifiedBy( dataSourceServiceId );
        if ( configModuleAssembly == null ) {
            module.entities( DataSourceConfiguration.class ).visibleIn( Visibility.layer );
        } else {
            configModuleAssembly.entities( DataSourceConfiguration.class ).visibleIn( Visibility.layer );
        }

    }

}
