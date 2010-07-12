/*
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
package org.qi4j.entitystore.sql.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLEntityStoreService;
import org.qi4j.entitystore.sql.database.DatabaseSQLServiceStatementsMixin;
import org.qi4j.entitystore.sql.database.DatabaseSQLService.DatabaseSQLServiceComposite;
import org.qi4j.entitystore.sql.database.DatabaseSQLServiceCoreMixin;
import org.qi4j.entitystore.sql.database.DatabaseSQLServiceSpi;
import org.qi4j.entitystore.sql.database.DatabaseSQLStringsBuilder;
import org.qi4j.entitystore.sql.database.MySQLDatabaseSQLServiceMixin;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public class MySQLEntityStoreAssembler
        implements Assembler
{

    public static final String SERVICE_NAME = "entitystore_mysql";

    private final Visibility _visibility;

    public MySQLEntityStoreAssembler()
    {
        this( Visibility.module );
    }

    public MySQLEntityStoreAssembler( Visibility _visibility )
    {
        this._visibility = _visibility;
    }

    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.addServices( SQLEntityStoreService.class ).
                visibleIn( this._visibility );

        module.addServices( DatabaseSQLServiceComposite.class ).
                withMixins( DatabaseSQLServiceCoreMixin.class,
                            DatabaseSQLServiceSpi.CommonMixin.class,
                            DatabaseSQLStringsBuilder.CommonMixin.class,
                            DatabaseSQLServiceStatementsMixin.class,
                            MySQLDatabaseSQLServiceMixin.class ).
                identifiedBy( SERVICE_NAME ).
                visibleIn( Visibility.module );

        module.addServices( UuidIdentityGeneratorService.class ).
                visibleIn( this._visibility );
    }

}
