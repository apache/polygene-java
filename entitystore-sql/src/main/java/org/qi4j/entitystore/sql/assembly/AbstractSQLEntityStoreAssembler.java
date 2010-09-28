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
package org.qi4j.entitystore.sql.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.entitystore.sql.internal.database.DatabaseSQLService.DatabaseSQLServiceComposite;
import org.qi4j.entitystore.sql.internal.database.DatabaseSQLServiceCoreMixin;
import org.qi4j.entitystore.sql.internal.database.DatabaseSQLServiceSpi;
import org.qi4j.entitystore.sql.internal.database.DatabaseSQLServiceStatementsMixin;
import org.qi4j.entitystore.sql.internal.database.DatabaseSQLStringsBuilder;
import org.qi4j.library.sql.common.AbstractSQLAssembler;
import org.qi4j.library.sql.ds.assembly.DataSourceAssembler;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
/* package */abstract class AbstractSQLEntityStoreAssembler extends AbstractSQLAssembler
{

    private static final Visibility DEFAULT_VISIBILITY = Visibility.module;

    public AbstractSQLEntityStoreAssembler()
    {
        this( DEFAULT_VISIBILITY, new DataSourceAssembler() );
    }

    public AbstractSQLEntityStoreAssembler( Visibility visibility )
    {
        this( visibility, new DataSourceAssembler() );
    }

    public AbstractSQLEntityStoreAssembler( DataSourceAssembler assembler )
    {
        this( DEFAULT_VISIBILITY, assembler );
    }

    public AbstractSQLEntityStoreAssembler( Visibility visibility, DataSourceAssembler assembler )
    {
        super( visibility, assembler );
    }

    protected abstract String getEntityStoreServiceName();

    protected abstract Class<?> getDatabaseSQLServiceSpecializationMixin();

    @SuppressWarnings("unchecked")
    public final void doAssemble( ModuleAssembly module )
        throws AssemblyException
    {

        module.addServices( SQLEntityStoreService.class ).visibleIn( this.getVisibility() );

        module
            .addServices( DatabaseSQLServiceComposite.class )
            .withMixins( DatabaseSQLServiceCoreMixin.class, DatabaseSQLServiceSpi.CommonMixin.class,
                DatabaseSQLStringsBuilder.CommonMixin.class, DatabaseSQLServiceStatementsMixin.class,
                getDatabaseSQLServiceSpecializationMixin() ).identifiedBy( getEntityStoreServiceName() )
            .visibleIn( Visibility.module );

        module.addServices( UuidIdentityGeneratorService.class ).visibleIn( this.getVisibility() );
    }

}
