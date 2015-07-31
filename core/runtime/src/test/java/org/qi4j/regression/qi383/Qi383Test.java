/*
 * Copyright 2013 Niclas Hedhman.
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
package org.qi4j.regression.qi383;

import org.junit.Test;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.test.AbstractQi4jTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

public class Qi383Test extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.addServices( MemoryEntityStoreService.class );
        module.entities( Car.class );
    }

    @Test( expected = EntityCompositeAlreadyExistsException.class )
    public void givenUnitOfWorkInProgressWhenAddingSameEntityTwiceExpectException()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork unitOfWork = module.newUnitOfWork() )
        {
            unitOfWork.newEntity( Car.class, "Ferrari" );
            unitOfWork.newEntity( Car.class, "Ford" );
            unitOfWork.newEntity( Car.class, "Ferrari" );
            unitOfWork.complete();
        }
    }

    public interface Car extends EntityComposite
    {
    }
}
