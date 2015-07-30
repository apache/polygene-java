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
package org.apache.zest.entitystore.riak;

import org.apache.zest.bootstrap.Assemblers;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.ServiceDeclaration;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;

/**
 * Riak Http EntityStore assembly.
 */
public class RiakHttpMapEntityStoreAssembler
    extends Assemblers.VisibilityIdentityConfig<RiakHttpMapEntityStoreAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( UuidIdentityGeneratorService.class ).
            visibleIn( visibility() );
        ServiceDeclaration service = module.services( RiakMapEntityStoreService.class ).
            withMixins( RiakHttpMapEntityStoreMixin.class ).
            visibleIn( visibility() );
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
        if( hasConfig() )
        {
            configModule().entities( RiakHttpEntityStoreConfiguration.class ).
                visibleIn( configVisibility() );
        }
    }
}
