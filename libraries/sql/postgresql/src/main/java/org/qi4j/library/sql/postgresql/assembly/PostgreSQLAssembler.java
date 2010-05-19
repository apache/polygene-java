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

package org.qi4j.library.sql.postgresql.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.library.sql.postgresql.PostgreSQLService;

/**
 * This is the assembler class to use when PostgreSQL is database for SQL Indexing in your application.
 *
 * @author Stanislav Muhametsin
 */
public class PostgreSQLAssembler implements Assembler
{

    /**
     * The default name for the service.
     */
    public static final String DEFAULT_SERVICE_NAME = "pgsql_lib";

    /**
     * The default visibility for the service.
     */
    public static final Visibility DEFAULT_VISIBILTY = Visibility.application;

    private Visibility _esVisiblity;

    private String _serviceName;

    public PostgreSQLAssembler( )
    {
        this(
            DEFAULT_VISIBILTY );
    }

    public PostgreSQLAssembler( Visibility entityStoreVisibility )
    {
        this(
            entityStoreVisibility,
            DEFAULT_SERVICE_NAME );
    }

    public PostgreSQLAssembler( Visibility entityStoreVisibility, String serviceName )
    {
        this._esVisiblity = entityStoreVisibility;
        this._serviceName = serviceName;
    }

    @Override
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( PostgreSQLService.class ).identifiedBy( this._serviceName ).visibleIn( this._esVisiblity ).instantiateOnStartup( );

        module.addServices( ReindexerService.class );
    }

}
