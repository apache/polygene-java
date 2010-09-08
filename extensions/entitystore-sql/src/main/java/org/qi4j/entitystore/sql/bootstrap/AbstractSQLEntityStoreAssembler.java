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
package org.qi4j.entitystore.sql.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.entitystore.sql.database.DatabaseSQLService.DatabaseSQLServiceComposite;
import org.qi4j.entitystore.sql.database.DatabaseSQLServiceCoreMixin;
import org.qi4j.entitystore.sql.database.DatabaseSQLServiceSpi;
import org.qi4j.entitystore.sql.database.DatabaseSQLServiceStatementsMixin;
import org.qi4j.entitystore.sql.database.DatabaseSQLStringsBuilder;
import org.qi4j.entitystore.sql.datasource.DBCPBasicDataSourceServiceMixin;
import org.qi4j.entitystore.sql.datasource.DataSourceService;
import org.qi4j.entitystore.sql.datasource.DataSourceServiceComposite;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
/* package */ abstract class AbstractSQLEntityStoreAssembler
        implements Assembler
{

    private static final Visibility DEFAULT_VISIBILITY = Visibility.module;

    private final Visibility visibility;

    private final DataSourceService importedDataSourceService;

    private final Class<?>[] dataSourceServiceMixins;

    public AbstractSQLEntityStoreAssembler()
    {
        this( DEFAULT_VISIBILITY );
    }

    @SuppressWarnings( "unchecked" )
    public AbstractSQLEntityStoreAssembler( Visibility visibility )
    {
        NullArgumentException.validateNotNull( "Visibility", visibility );
        this.visibility = visibility;
        this.importedDataSourceService = null;
        this.dataSourceServiceMixins = new Class<?>[]{ DBCPBasicDataSourceServiceMixin.class };
    }

    public AbstractSQLEntityStoreAssembler( DataSourceService importedDataSourceService )
    {
        this( DEFAULT_VISIBILITY, importedDataSourceService );
    }

    public AbstractSQLEntityStoreAssembler( Visibility visibility, DataSourceService importedDataSourceService )
    {
        NullArgumentException.validateNotNull( "Visibility", visibility );
        this.visibility = visibility;
        this.importedDataSourceService = importedDataSourceService;
        this.dataSourceServiceMixins = null;
    }

    public AbstractSQLEntityStoreAssembler( Class<? extends DataSourceService>... dataSourceServiceMixins )
    {
        this( DEFAULT_VISIBILITY, dataSourceServiceMixins );
    }

    public AbstractSQLEntityStoreAssembler( Visibility visibility, Class<? extends DataSourceService>... dataSourceServiceMixins )
    {
        NullArgumentException.validateNotNull( "Visibility", visibility );
        NullArgumentException.validateNotNull( "DataSourceService Mixins", dataSourceServiceMixins );
        this.visibility = visibility;
        this.importedDataSourceService = null;
        this.dataSourceServiceMixins = dataSourceServiceMixins;
    }

    protected abstract String getEntityStoreServiceName();

    protected abstract String getDataSourceServiceName();

    protected abstract Class<?> getDatabaseSQLServiceSpecializationMixin();

    @SuppressWarnings( "unchecked" )
    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {

        if ( importedDataSourceService != null ) {

            // Imported DataSourceService
            module.importServices( DataSourceService.class ).
                    identifiedBy( getDataSourceServiceName() ).
                    setMetaInfo( importedDataSourceService );

        } else if ( dataSourceServiceMixins != null && dataSourceServiceMixins.length > 0 ) {

            // Parametrized DataSourceService
            module.addServices( DataSourceServiceComposite.class ).
                    withMixins( dataSourceServiceMixins ).
                    identifiedBy( getDataSourceServiceName() ).
                    instantiateOnStartup();

        } else {
            throw new IllegalStateException( "Unable to assemble SQLEntityStore, no importable DataSourceService nor DataSourceServiceMixin provided" );
        }

        module.addServices( SQLEntityStoreService.class ).
                visibleIn( this.visibility );

        module.addServices( DatabaseSQLServiceComposite.class ).
                withMixins( DatabaseSQLServiceCoreMixin.class,
                            DatabaseSQLServiceSpi.CommonMixin.class,
                            DatabaseSQLStringsBuilder.CommonMixin.class,
                            DatabaseSQLServiceStatementsMixin.class,
                            getDatabaseSQLServiceSpecializationMixin() ).
                identifiedBy( getEntityStoreServiceName() ).
                visibleIn( Visibility.module );

        module.addServices( UuidIdentityGeneratorService.class ).
                visibleIn( this.visibility );
    }

}
