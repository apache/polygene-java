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
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.EntityCompositeAlreadyExistsException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

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
