/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql.assembly;

import java.io.IOException;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.entitystore.sql.internal.DatabaseSQLService.DatabaseSQLServiceComposite;
import org.qi4j.entitystore.sql.internal.DatabaseSQLServiceCoreMixin;
import org.qi4j.entitystore.sql.internal.DatabaseSQLServiceSpi;
import org.qi4j.entitystore.sql.internal.DatabaseSQLServiceStatementsMixin;
import org.qi4j.entitystore.sql.internal.DatabaseSQLStringsBuilder;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * Base SQL EntityStore assembly.
 */
@SuppressWarnings( "unchecked" )
abstract class AbstractSQLEntityStoreAssembler<AssemblerType>
    extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{

    public static final String DEFAULT_ENTITYSTORE_IDENTITY = "entitystore-sql";

    protected SQLVendor getSQLVendor()
        throws IOException
    {
        return SQLVendorProvider.createVendor( SQLVendor.class );
    }

    protected Class<?> getDatabaseStringBuilderMixin()
    {
        return DatabaseSQLStringsBuilder.CommonMixin.class;
    }

    protected abstract Class<?> getDatabaseSQLServiceSpecializationMixin();

    @Override
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        try
        {
            SQLVendor sqlVendor = this.getSQLVendor();
            if( sqlVendor == null )
            {
                throw new AssemblyException( "SQL Vendor could not be determined." );
            }
            module.services( DatabaseSQLServiceComposite.class ).
                withMixins( DatabaseSQLServiceCoreMixin.class,
                            DatabaseSQLServiceSpi.CommonMixin.class,
                            getDatabaseStringBuilderMixin(),
                            DatabaseSQLServiceStatementsMixin.class,
                            getDatabaseSQLServiceSpecializationMixin() ).
                identifiedBy( hasIdentity() ? identity() : DEFAULT_ENTITYSTORE_IDENTITY ).
                visibleIn( Visibility.module ).
                setMetaInfo( sqlVendor );
        }
        catch( IOException ioe )
        {
            throw new AssemblyException( ioe );
        }
        module.services( SQLEntityStoreService.class,
                         UuidIdentityGeneratorService.class ).
            visibleIn( visibility() );
        if( hasConfig() )
        {
            configModule().entities( SQLConfiguration.class ).
                visibleIn( configVisibility() );
        }
    }

}
