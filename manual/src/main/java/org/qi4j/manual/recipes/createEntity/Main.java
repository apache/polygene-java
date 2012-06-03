package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {

        Energy4Java qi4j = new Energy4Java();

        // Instantiate the Application Model.
        Application application = qi4j.newApplication( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory) throws AssemblyException
            {
                return applicationFactory.newApplicationAssembly( new MyAssembler()) ;
            }
        });

        application.activate();

    }
}
