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

import org.qi4j.entitystore.sql.map.database.DatabaseDerbyMixin;
import org.qi4j.entitystore.sql.map.database.DatabaseService;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.sql.SQLMapEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class DerbySQLMapEntityStoreAssembler
        implements Assembler
{

    private final Visibility visibility;

    public DerbySQLMapEntityStoreAssembler()
    {
        this( Visibility.module );
    }

    public DerbySQLMapEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @SuppressWarnings( { "unchecked" } )
    public void assemble( ModuleAssembly ma )
            throws AssemblyException
    {
        ma.addServices( DatabaseService.class ).
                withMixins( DatabaseDerbyMixin.class ).
                identifiedBy( "entitystore-sql-derby" ).
                visibleIn( Visibility.module );
        ma.addServices( SQLMapEntityStoreService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();
        ma.addServices( UuidIdentityGeneratorService.class ).
                visibleIn( visibility );
    }

}
