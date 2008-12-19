/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.entity.neo4j;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.entity.neo4j.state.DirectEntityStateFactory;
import org.qi4j.entity.neo4j.state.IndirectEntityStateFactory;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.api.common.Visibility;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;

public class Neo4jEntityStoreAssembler
    implements Assembler
{
    private String neo4jConfigModuleName;

    public Neo4jEntityStoreAssembler( String neo4jConfigModuleName )
    {
        this.neo4jConfigModuleName = neo4jConfigModuleName;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices(
            NeoEntityStoreService.class,
            NeoCoreService.class,
            DirectEntityStateFactory.class,
            IndirectEntityStateFactory.class,
            NeoIdentityIndexService.class,
            UuidIdentityGeneratorService.class
        );

        ModuleAssembly config = module.layerAssembly().newModuleAssembly( neo4jConfigModuleName );
        config.addEntities( NeoCoreConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
    }
}
