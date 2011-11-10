/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.sql.ds.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.sql.ds.DBCPBasicDataSourceServiceMixin;
import org.qi4j.library.sql.ds.DataSourceService;
import org.qi4j.library.sql.ds.DataSourceServiceComposite;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class DataSourceAssembler
    implements Assembler
{

    private static final Visibility DEFAULT_VISIBILITY = Visibility.module;

    private final Visibility visibility;

    private final DataSourceService importedDataSourceService;

    private final Class<?>[] dataSourceServiceMixins;

    private String serviceName;

    public DataSourceAssembler()
    {
        this( DEFAULT_VISIBILITY );
    }

    @SuppressWarnings("unchecked")
    public DataSourceAssembler( Visibility visibility )
    {
        this( visibility, DBCPBasicDataSourceServiceMixin.class );
    }

    public DataSourceAssembler( DataSourceService importedDataSourceService )
    {
        this( DEFAULT_VISIBILITY, importedDataSourceService );
    }

    public DataSourceAssembler( Visibility visibility, DataSourceService importedDataSourceService )
    {
        NullArgumentException.validateNotNull( "Visibility", visibility );
        this.visibility = visibility;
        this.importedDataSourceService = importedDataSourceService;
        this.dataSourceServiceMixins = null;
    }

    public DataSourceAssembler( Class<? extends DataSourceService>... dataSourceServiceMixins )
    {
        this( DEFAULT_VISIBILITY, dataSourceServiceMixins );
    }

    public DataSourceAssembler( Visibility visibility, Class<? extends DataSourceService>... dataSourceServiceMixins )
    {
        NullArgumentException.validateNotNull( "Visibility", visibility );
        NullArgumentException.validateNotNull( "DataSourceService Mixins", dataSourceServiceMixins );
        this.visibility = visibility;
        this.importedDataSourceService = null;
        this.dataSourceServiceMixins = dataSourceServiceMixins;
    }

    protected String getDataSourceServiceName()
    {
        return this.serviceName;
    }

    public DataSourceAssembler setDataSourceServiceName( String newName )
    {
        this.serviceName = newName;
        return this;
    }

    @SuppressWarnings("unchecked")
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {

        if( importedDataSourceService != null )
        {

            // Imported DataSourceService
            module.importedServices( DataSourceService.class ).identifiedBy( getDataSourceServiceName() )
                .visibleIn( this.visibility ).setMetaInfo( importedDataSourceService );

        }
        else if( dataSourceServiceMixins != null && dataSourceServiceMixins.length > 0 )
        {

            // Parametrized DataSourceService
            module.services( DataSourceServiceComposite.class ).withMixins( dataSourceServiceMixins )
                .visibleIn( this.visibility ).identifiedBy( getDataSourceServiceName() ).instantiateOnStartup();

        }
        else
        {
            throw new IllegalStateException(
                "Unable to assemble SQLEntityStore, no importable DataSourceService nor DataSourceServiceMixin provided" );
        }
    }

}
