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
package org.qi4j.entitystore.riak;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * Riak Protobuf EntityStore assembly.
 */
public class RiakProtobufMapEntityStoreAssembler
    implements Assembler
{

    private Visibility visibility = Visibility.application;
    private ModuleAssembly configModule;
    private Visibility configVisibility = Visibility.layer;

    public RiakProtobufMapEntityStoreAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public RiakProtobufMapEntityStoreAssembler withConfigModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return this;
    }

    public RiakProtobufMapEntityStoreAssembler withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( UuidIdentityGeneratorService.class ).
            visibleIn( visibility );

        module.services( RiakMapEntityStoreService.class ).
            withMixins( RiakProtobufMapEntityStoreMixin.class ).
            visibleIn( visibility );

        if( configModule != null )
        {
            configModule.entities( RiakProtobufEntityStoreConfiguration.class ).
                visibleIn( configVisibility );
        }
    }

}
