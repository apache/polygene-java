/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.index.solr;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;

import static org.qi4j.api.service.qualifier.ServiceTags.*;

/**
 * JAVADOC
 */
public class SolrAssembler
   implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.services( EmbeddedSolrService.class ).identifiedBy( "solr" ).instantiateOnStartup();

      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor( "search", "" );
      namedQueries.addQuery( queryDescriptor );
      module.services( SolrQueryService.class ).
            taggedWith( "solr","search" ).
            setMetaInfo( namedQueries ).
            identifiedBy( "solrquery" ).
            visibleIn( Visibility.application );

      module.objects( EntityStateSerializer.class );
   }
}
