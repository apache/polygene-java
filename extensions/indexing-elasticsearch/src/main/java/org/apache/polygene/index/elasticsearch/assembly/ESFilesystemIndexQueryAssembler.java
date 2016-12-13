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
package org.apache.polygene.index.elasticsearch.assembly;

import org.apache.polygene.api.value.ValueSerialization;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.elasticsearch.ElasticSearchConfiguration;
import org.apache.polygene.index.elasticsearch.filesystem.ESFilesystemIndexQueryService;
import org.apache.polygene.index.elasticsearch.internal.AbstractElasticSearchAssembler;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationService;

public class ESFilesystemIndexQueryAssembler
    extends AbstractElasticSearchAssembler<ESFilesystemIndexQueryAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( ESFilesystemIndexQueryService.class ).
            identifiedBy( identity().toString() ).
            visibleIn( visibility() ).
            instantiateOnStartup();

        module.services( OrgJsonValueSerializationService.class ).
            taggedWith( ValueSerialization.Formats.JSON );

        if( hasConfig() )
        {
            configModule().entities( ElasticSearchConfiguration.class ).
                visibleIn( configVisibility() );
        }
    }
}
