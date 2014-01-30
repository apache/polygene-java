/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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
package org.qi4j.test.indexing;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.model.Address;
import org.qi4j.test.indexing.model.File;
import org.qi4j.test.indexing.model.Host;
import org.qi4j.test.indexing.model.Port;
import org.qi4j.test.indexing.model.Protocol;
import org.qi4j.test.indexing.model.QueryParam;
import org.qi4j.test.indexing.model.URL;
import org.qi4j.test.indexing.model.entities.AccountEntity;
import org.qi4j.test.indexing.model.entities.CatEntity;
import org.qi4j.test.indexing.model.entities.CityEntity;
import org.qi4j.test.indexing.model.entities.DomainEntity;
import org.qi4j.test.indexing.model.entities.FemaleEntity;
import org.qi4j.test.indexing.model.entities.MaleEntity;

/**
 * Abstract satisfiedBy with tests for any queries against Index/Query engines.
 */
public class AbstractAnyQueryTest
    extends AbstractQi4jTest
{
    protected UnitOfWork unitOfWork;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( MaleEntity.class,
                         FemaleEntity.class,
                         CityEntity.class,
                         DomainEntity.class,
                         AccountEntity.class,
                         CatEntity.class );
        module.values( URL.class,
                       Address.class,
                       Protocol.class,
                       Host.class,
                       Port.class,
                       File.class,
                       QueryParam.class );

        new EntityTestAssembler().assemble( module );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        TestData.populate( module );

        this.unitOfWork = this.module.newUnitOfWork();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        if( this.unitOfWork != null )
        {
            this.unitOfWork.discard();
            this.unitOfWork = null;
        }
        super.tearDown();
    }
}
