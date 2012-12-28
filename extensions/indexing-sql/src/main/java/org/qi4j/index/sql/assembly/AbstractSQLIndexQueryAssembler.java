/*
 * Copyright (c) 2010, Stanislav Muhametsin.
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.index.sql.assembly;

import java.io.IOException;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.reindexer.ReindexerConfiguration;
import org.qi4j.index.reindexer.ReindexerService;
import org.qi4j.index.sql.support.common.ReindexingStrategy;
import org.qi4j.library.sql.common.SQLConfiguration;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

public abstract class AbstractSQLIndexQueryAssembler<T extends AbstractSQLIndexQueryAssembler>
        implements Assembler
{

    public static final String DEFAULT_IDENTITY = "indexing-sql";

    private String identity = DEFAULT_IDENTITY;

    private Visibility visibility = Visibility.module;

    private ModuleAssembly configModule;

    private Visibility configVisibility = Visibility.module;

    private Class<? extends ReindexingStrategy> reindexingStrategy = ReindexingStrategy.NeverNeed.class;

    public T identifiedBy( String identity )
    {
        this.identity = identity;
        return ( T ) this;
    }

    public T visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return ( T ) this;
    }

    public T withConfig( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return ( T ) this;
    }

    public T withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return ( T ) this;
    }

    public T withReindexingStrategy( Class<? extends ReindexingStrategy> reindexingStrategy )
    {
        this.reindexingStrategy = reindexingStrategy;
        return ( T ) this;
    }

    protected SQLVendor getSQLVendor()
            throws IOException
    {
        return SQLVendorProvider.createVendor( SQLVendor.class );
    }

    protected abstract Class<?> getIndexQueryServiceType();

    @Override
    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        if ( configModule == null ) {
            configModule = module;
        }
        try {
            SQLVendor sqlVendor = getSQLVendor();
            if ( sqlVendor == null ) {
                throw new AssemblyException( "SQL Vendor could not be determined." );
            }
            module.services( getIndexQueryServiceType() )
                    .identifiedBy( identity )
                    .setMetaInfo( sqlVendor )
                    .visibleIn( visibility )
                    .instantiateOnStartup();
        } catch ( IOException ex ) {
            throw new AssemblyException( "SQL Vendor could not be created", ex );
        }

        module.services( ReindexerService.class ).
                visibleIn( Visibility.module );
        module.services( ReindexingStrategy.class ).
                withMixins( reindexingStrategy ).
                visibleIn( Visibility.module );

        configModule.entities( SQLConfiguration.class,
                               ReindexerConfiguration.class ).
                visibleIn( configVisibility );
    }

}
