/*
 * Copyright 2009 Paul Merlin.
 * Copyright 2011 Niclas Hedhman.
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
package org.qi4j.entitystore.hazelcast.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.hazelcast.HazelcastConfiguration;
import org.qi4j.entitystore.hazelcast.HazelcastEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class HazelcastEntityStoreAssembler
    implements Assembler
{

    private final Visibility visibility;
    private ModuleAssembly config;
    private Visibility configVisibility;

    public HazelcastEntityStoreAssembler()
    {
        this( Visibility.application );
    }

    public HazelcastEntityStoreAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    public HazelcastEntityStoreAssembler withConfigIn( ModuleAssembly config, Visibility configVisibility )
    {
        this.config = config;
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( HazelcastEntityStoreService.class ).visibleIn( visibility ).instantiateOnStartup();
        module.services( UuidIdentityGeneratorService.class ).visibleIn( visibility );
        if( config != null )
        {
            config.entities( HazelcastConfiguration.class ).visibleIn( configVisibility );
        }
    }
}
