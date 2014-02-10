/*
 * Copyright 2011 Paul Merlin.
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
package org.qi4j.entitystore.mongodb;

import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class MongoMapEntityStoreAssembler
    extends Assemblers.VisibilityIdentityConfig<MongoMapEntityStoreAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( UuidIdentityGeneratorService.class ).visibleIn( visibility() );
        ServiceDeclaration service = module.services( MongoMapEntityStoreService.class ).visibleIn( visibility() );
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
        if( hasConfig() )
        {
            configModule().entities( MongoEntityStoreConfiguration.class ).visibleIn( configVisibility() );
        }
    }
}
