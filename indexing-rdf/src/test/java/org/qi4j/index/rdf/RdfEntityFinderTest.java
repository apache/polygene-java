/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.test.indexing.AbstractEntityFinderTest;

public class RdfEntityFinderTest extends AbstractEntityFinderTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
        module.services( RdfIndexingEngineService.class );
        module.services( MemoryRepositoryService.class ).identifiedBy( "rdf-indexing" );
        // module.services( NativeRdfRepositoryService.class ).identifiedBy( "rdf-indexing" );
        // module.addComposites( NativeRdfConfiguration.class );
    }
}