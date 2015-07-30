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

package org.apache.zest.index.solr;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.rdf.entity.EntityStateSerializer;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

/**
 * JAVADOC
 */
public class SolrAssembler
   implements Assembler
{
    @Override
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.services( EmbeddedSolrService.class ).identifiedBy( "solr" ).instantiateOnStartup();

      module.services( SolrQueryService.class ).
            taggedWith( "solr", "search" ).
            identifiedBy( "solrquery" ).
            visibleIn( Visibility.application );
      module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
      module.objects( EntityStateSerializer.class );
   }
}
