/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.index.rdf;

import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import static org.qi4j.index.rdf.Network.populate;
import org.qi4j.index.rdf.assembly.RdfFactoryService;
import org.qi4j.index.rdf.model.File;
import org.qi4j.index.rdf.model.Host;
import org.qi4j.index.rdf.model.Port;
import org.qi4j.index.rdf.model.Protocol;
import org.qi4j.index.rdf.model.QueryParam;
import org.qi4j.index.rdf.model.URL;
import org.qi4j.index.rdf.model.Address;
import org.qi4j.index.rdf.model.entities.AccountEntity;
import org.qi4j.index.rdf.model.entities.CatEntity;
import org.qi4j.index.rdf.model.entities.CityEntity;
import org.qi4j.index.rdf.model.entities.DomainEntity;
import org.qi4j.index.rdf.model.entities.FemaleEntity;
import org.qi4j.index.rdf.model.entities.MaleEntity;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.entity.typeregistry.EntityTypeRegistryService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class RdfEntityIndexerTest
{
    @Test
    public void script01() throws UnitOfWorkCompletionException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class );
                module.addEntities(
                    MaleEntity.class,
                    FemaleEntity.class,
                    CityEntity.class,
                    DomainEntity.class,
                    AccountEntity.class,
                    CatEntity.class
                );
                module.addValues(
                    URL.class,
                    Address.class,
                    Protocol.class,
                    Host.class,
                    Port.class,
                    File.class,
                    QueryParam.class
                );
                module.addServices(
                    MemoryEntityStoreService.class,
                    UuidIdentityGeneratorService.class,
                    RdfIndexerExporterComposite.class,
                    RdfFactoryService.class,
                    MemoryRepositoryService.class,
                    EntityTypeRegistryService.class
                );
            }
        };
        populate( assembler );
        assembler.serviceFinder().<RdfIndexerExporterComposite>findService( RdfIndexerExporterComposite.class ).get().toRDF( System.out );
    }
}
