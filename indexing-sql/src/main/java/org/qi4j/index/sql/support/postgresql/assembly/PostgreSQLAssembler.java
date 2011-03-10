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

package org.qi4j.index.sql.support.postgresql.assembly;

import java.io.IOException;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.index.sql.support.postgresql.PostgreSQLService;
import org.qi4j.library.sql.common.AbstractSQLAssembler;
import org.qi4j.library.sql.ds.assembly.DataSourceAssembler;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * This is the assembler class to use when PostgreSQL is database for SQL Indexing in your application.
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLAssembler extends AbstractSQLAssembler
{

    /**
     * The default name for the service.
     */
    public static final String INDEXING_SERVICE_NAME = "indexing_pgsql";

    public static final String DATASOURCE_SERVICE_NAME = "datasource_pgsql_indexing";

    /**
     * The default visibility for the service.
     */
    public static final Visibility DEFAULT_VISIBILITY = Visibility.application;

    private String _serviceName;

    public PostgreSQLAssembler()
    {
        this( DEFAULT_VISIBILITY, new DataSourceAssembler().setDataSourceServiceName( DATASOURCE_SERVICE_NAME ) );
    }

    public PostgreSQLAssembler( Visibility visibility )
    {
        this( visibility, new DataSourceAssembler().setDataSourceServiceName( DATASOURCE_SERVICE_NAME ) );
    }

    public PostgreSQLAssembler( DataSourceAssembler assembler )
    {
        this( DEFAULT_VISIBILITY, assembler );
    }

    public PostgreSQLAssembler( Visibility visibility, DataSourceAssembler assembler )
    {
        super( visibility, assembler );
    }

    public PostgreSQLAssembler setServiceName( String serviceName )
    {
        this._serviceName = serviceName;
        return this;
    }

    @Override
    protected void doAssemble( ModuleAssembly module )
        throws AssemblyException
    {
        if( this._serviceName == null )
        {
            this._serviceName = INDEXING_SERVICE_NAME;
        }

        try
        {
            module.services( PostgreSQLService.class ).identifiedBy( this._serviceName )
                .visibleIn( this.getVisibility() ).instantiateOnStartup()
                .setMetaInfo( SQLVendorProvider.createVendor( PostgreSQLVendor.class ) );
        }
        catch( IOException ioe )
        {
            throw new AssemblyException( ioe );
        }

        module.services( ReindexerService.class );
    }

}
