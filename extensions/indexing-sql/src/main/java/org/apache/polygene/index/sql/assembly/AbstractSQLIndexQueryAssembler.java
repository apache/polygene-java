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
package org.apache.polygene.index.sql.assembly;

import java.io.IOException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.reindexer.ReindexerConfiguration;
import org.apache.polygene.index.reindexer.ReindexerService;
import org.apache.polygene.index.sql.SQLIndexingEngineConfiguration;
import org.apache.polygene.index.sql.support.common.ReindexingStrategy;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendorProvider;

public abstract class AbstractSQLIndexQueryAssembler<AssemblerType>
    extends Assemblers.VisibilityIdentityConfig<AssemblerType>
{
    public static final Identity DEFAULT_IDENTITY = new StringIdentity( "indexing-sql" );

    private Class<? extends ReindexingStrategy> reindexingStrategy = ReindexingStrategy.NeverNeed.class;

    public AbstractSQLIndexQueryAssembler()
    {
        identifiedBy( DEFAULT_IDENTITY.toString() );
    }

    @SuppressWarnings( "unchecked" )
    public AssemblerType withReindexingStrategy( Class<? extends ReindexingStrategy> reindexingStrategy )
    {
        this.reindexingStrategy = reindexingStrategy;
        return (AssemblerType) this;
    }

    protected SQLVendor getSQLVendor()
        throws IOException
    {
        return SQLVendorProvider.createVendor( SQLVendor.class );
    }

    protected abstract Class<?> getIndexQueryServiceType();

    @Override
    public final void assemble( ModuleAssembly module )
    {
        try
        {
            SQLVendor sqlVendor = getSQLVendor();
            if( sqlVendor == null )
            {
                throw new AssemblyException( "SQL Vendor could not be determined." );
            }
            module.services( getIndexQueryServiceType() )
                  .taggedWith( "sql", "query", "indexing" )
                  .identifiedBy( identity() )
                  .setMetaInfo( sqlVendor )
                  .visibleIn( visibility() )
                  .instantiateOnStartup();
        }
        catch( IOException ex )
        {
            throw new AssemblyException( "SQL Vendor could not be created", ex );
        }

        module.services( ReindexerService.class ).
            visibleIn( Visibility.module );
        module.services( ReindexingStrategy.class ).
            withMixins( reindexingStrategy ).
                  visibleIn( Visibility.module );

        if( hasConfig() )
        {
            configModule().entities( SQLIndexingEngineConfiguration.class,
                                     ReindexerConfiguration.class ).
                              visibleIn( configVisibility() );
        }
    }
}
