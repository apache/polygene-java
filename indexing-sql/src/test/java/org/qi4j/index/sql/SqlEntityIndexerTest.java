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
package org.qi4j.index.sql;

import java.io.IOException;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.sql.internal.EntityStateSqlSerializer;
import org.qi4j.index.sql.model.Address;
import org.qi4j.index.sql.model.File;
import org.qi4j.index.sql.model.Host;
import org.qi4j.index.sql.model.Port;
import org.qi4j.index.sql.model.Protocol;
import org.qi4j.index.sql.model.QueryParam;
import org.qi4j.index.sql.model.URL;
import org.qi4j.index.sql.model.entities.AccountEntity;
import org.qi4j.index.sql.model.entities.CatEntity;
import org.qi4j.index.sql.model.entities.CityEntity;
import org.qi4j.index.sql.model.entities.DomainEntity;
import org.qi4j.index.sql.model.entities.FemaleEntity;
import org.qi4j.index.sql.model.entities.MaleEntity;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import static org.qi4j.index.sql.Network.*;

public class SqlEntityIndexerTest
{
    @Test
    public void script01()
        throws UnitOfWorkCompletionException, IOException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addObjects( EntityStateSqlSerializer.class );
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
                module.addServices( MemoryEntityStoreService.class );
                module.addServices( UuidIdentityGeneratorService.class );
                module.addServices( SqlIndexerExporterComposite.class );
                module.addServices( SqlFactoryService.class );
            }
        };
        populate( assembler );
        assembler.serviceFinder()
            .<SqlIndexerExporterComposite>findService( SqlIndexerExporterComposite.class )
            .get()
            .toSQL( System.out );
    }
}
