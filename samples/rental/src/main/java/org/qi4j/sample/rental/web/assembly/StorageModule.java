/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.sample.rental.web.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.functional.Function;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class StorageModule
    implements Assembler
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MemoryEntityStoreService.class ).visibleIn( Visibility.application );
        module.services( UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
        new OrgJsonValueSerializationAssembler().
            visibleIn( Visibility.application ).
            withValuesModuleFinder( new Function<Application, Module>()
        {
            @Override
            public Module map( Application app )
            {
                return app.findModule( "DomainLayer", "RentalModule" );
            }
        } ).
            assemble( module );
    }
}
