/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.entitystore.sql.assembly;

import java.io.IOException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.sql.SQLEntityStoreService;
import org.apache.polygene.entitystore.sql.internal.DatabaseSQLService.DatabaseSQLServiceComposite;
import org.apache.polygene.entitystore.sql.internal.DatabaseSQLServiceCoreMixin;
import org.apache.polygene.entitystore.sql.internal.DatabaseSQLServiceSpi;
import org.apache.polygene.entitystore.sql.internal.DatabaseSQLServiceStatementsMixin;
import org.apache.polygene.entitystore.sql.internal.DatabaseSQLStringsBuilder;
import org.apache.polygene.library.sql.common.SQLConfiguration;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * Base SQL EntityStore assembly.
 */
@SuppressWarnings( "unchecked" )
abstract class AbstractSQLEntityStoreAssembler<AssemblerType>
    extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{

    public static final Identity DEFAULT_ENTITYSTORE_IDENTITY = new StringIdentity( "entitystore-sql" );

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
            module.services( DatabaseSQLServiceComposite.class )
                    .withMixins( DatabaseSQLServiceCoreMixin.class,
                            DatabaseSQLServiceSpi.CommonMixin.class,
                            getDatabaseStringBuilderMixin(),
                            DatabaseSQLServiceStatementsMixin.class,
                            getDatabaseSQLServiceSpecializationMixin() )
                    .identifiedBy( ( hasIdentity() ? identity().toString() : DEFAULT_ENTITYSTORE_IDENTITY ).toString() ).
                visibleIn( Visibility.module ).
                setMetaInfo( sqlVendor );
        }
        catch( IOException ioe )
        {
            throw new AssemblyException( ioe );
        }
        module.services( SQLEntityStoreService.class ).visibleIn( visibility() );
        if( hasConfig() )
        {
            configModule().entities( SQLConfiguration.class ).visibleIn( configVisibility() );
        }
    }
}
