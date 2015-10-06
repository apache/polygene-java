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
package org.apache.zest.test.indexing;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.indexing.model.Address;
import org.apache.zest.test.indexing.model.File;
import org.apache.zest.test.indexing.model.Host;
import org.apache.zest.test.indexing.model.Port;
import org.apache.zest.test.indexing.model.Protocol;
import org.apache.zest.test.indexing.model.QueryParam;
import org.apache.zest.test.indexing.model.URL;
import org.apache.zest.test.indexing.model.entities.AccountEntity;
import org.apache.zest.test.indexing.model.entities.CatEntity;
import org.apache.zest.test.indexing.model.entities.CityEntity;
import org.apache.zest.test.indexing.model.entities.DomainEntity;
import org.apache.zest.test.indexing.model.entities.FemaleEntity;
import org.apache.zest.test.indexing.model.entities.MaleEntity;

/**
 * Abstract satisfiedBy with tests for any queries against Index/Query engines.
 */
public class AbstractAnyQueryTest
    extends AbstractZestTest
{
    protected UnitOfWork unitOfWork;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        assembleEntities( module, Visibility.module );
        assembleValues( module, Visibility.module );
        new EntityTestAssembler().assemble( module );
    }

    protected void assembleEntities( ModuleAssembly module, Visibility visibility )
    {
        module.entities( MaleEntity.class,
                         FemaleEntity.class,
                         CityEntity.class,
                         DomainEntity.class,
                         AccountEntity.class,
                         CatEntity.class ). visibleIn( visibility );
    }

    protected void assembleValues( ModuleAssembly module, Visibility visibility )
    {
        module.values( URL.class,
                       Address.class,
                       Protocol.class,
                       Host.class,
                       Port.class,
                       File.class,
                       QueryParam.class ).visibleIn( visibility );
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
