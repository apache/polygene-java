/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.entitystore.redis;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * Redis EntityStore assembly.
 */
public class RedisMapEntityStoreAssembler
    implements Assembler
{

    private Visibility visibility = Visibility.application;
    private ModuleAssembly configModule;
    private Visibility configVisibility = Visibility.layer;

    public RedisMapEntityStoreAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public RedisMapEntityStoreAssembler withConfigModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return this;
    }

    public RedisMapEntityStoreAssembler withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( RedisMapEntityStoreService.class ).visibleIn( visibility );
        module.services( UuidIdentityGeneratorService.class ).visibleIn( visibility );
        if( configModule != null )
        {
            configModule.entities( RedisEntityStoreConfiguration.class ).visibleIn( configVisibility );
        }
    }

}
