package org.qi4j.tests.regression.qi383;

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
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            unitOfWork.newEntity( Car.class, "Ferrari" );
            unitOfWork.newEntity( Car.class, "Ford" );
            unitOfWork.newEntity( Car.class, "Ferrari" );
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    public interface Car extends EntityComposite
    {
    }
}
