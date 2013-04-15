package org.qi4j.manual.recipes.assemble;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;

public class Docs
{
    @This
    Module module;

    public void showUsage()
    {
        {
// START SNIPPET: indirect
            UnitOfWork unitOfWork = module.currentUnitOfWork();
            Person person = unitOfWork.newEntity( Person.class );
// END SNIPPET: indirect
        }
        {
// START SNIPPET: direct
            UnitOfWork unitOfWork = module.currentUnitOfWork();
            PersonEntity person = unitOfWork.newEntity( PersonEntity.class );
// END SNIPPET: direct
        }
    }

    public interface Person
    {
    }

    public interface PersonEntity extends ValueComposite
    {
    }
}
