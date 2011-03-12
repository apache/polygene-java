/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.index.rdf.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.NativeRepositoryService;
import org.qi4j.spi.query.NamedQueries;

public class RdfNativeSesameStoreAssembler
    implements Assembler
{
    private NamedQueries namedQueries;
    private Visibility indexingVisibility;
    private Visibility repositoryVisibility;

    public RdfNativeSesameStoreAssembler( NamedQueries namedQueries )
    {
        this( namedQueries, Visibility.application, Visibility.module );
    }

    public RdfNativeSesameStoreAssembler()
    {
        this( null, Visibility.application, Visibility.module );
    }

    public RdfNativeSesameStoreAssembler( NamedQueries namedQueries,
                                          Visibility indexingVisibility,
                                          Visibility repositoryVisibility
    )
    {
        if( namedQueries == null )
        {
            namedQueries = new NamedQueries();
        }
        this.namedQueries = namedQueries;
        this.indexingVisibility = indexingVisibility;
        this.repositoryVisibility = repositoryVisibility;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( NativeRepositoryService.class )
            .visibleIn( repositoryVisibility )
            .instantiateOnStartup()
            .identifiedBy( "rdf-indexing" );
        module.services( RdfIndexingEngineService.class )
            .visibleIn( indexingVisibility )
            .setMetaInfo( namedQueries )
            .instantiateOnStartup();
        module.services( RdfQueryParserFactory.class ).visibleIn( indexingVisibility );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
    }
}
