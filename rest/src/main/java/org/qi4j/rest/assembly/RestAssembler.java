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
package org.qi4j.rest.assembly;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.rest.Qi4jEntityFinder;
import org.qi4j.rest.RestApplication;
import org.qi4j.rest.SPARQLResource;
import org.qi4j.rest.entity.AllEntitiesResource;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.index.IndexResource;
import org.qi4j.rest.type.EntityTypeResource;
import org.qi4j.rest.type.EntityTypesResource;
import org.qi4j.structure.Visibility;

public class RestAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects( RestApplication.class ).visibleIn( Visibility.application );
        module.addObjects( Qi4jEntityFinder.class,
                           AllEntitiesResource.class,
                           EntitiesResource.class,
                           EntityResource.class,
                           EntityTypesResource.class,
                           EntityTypeResource.class,
                           IndexResource.class,
                           SPARQLResource.class );
    }
}
