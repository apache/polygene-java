package org.qi4j.manual.travel;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        SingletonAssembler singleton = new SingletonAssembler()
        {
            @Override
// START SNIPPET: assemble
            public void assemble(ModuleAssembly module) throws AssemblyException
            {
                module.addServices(TravelPlanService.class)
                        .instantiateOnStartup()
                        .identifiedBy("ExpediaService");

                module.addServices(TravelPlanService.class)
                        .instantiateOnStartup()
                        .identifiedBy("OrbitzService");
            }
// END SNIPPET: assemble
        };
    }

    public static void simple(String[] args) throws Exception
    {
        SingletonAssembler singleton = new SingletonAssembler()
        {
            @Override
// START SNIPPET: simple
            public void assemble(ModuleAssembly module) throws AssemblyException
            {
                module.addServices(TravelPlanService.class).instantiateOnStartup();
            }
// END SNIPPET: simple
        };
    }

}