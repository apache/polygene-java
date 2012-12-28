package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;

// START SNIPPET: assembler2
// START SNIPPET: assembler1
public class MyAssembler
        implements Assembler
{
    public void assemble( ModuleAssembly module )
    {
        module.entities( CarEntity.class,
                ManufacturerEntity.class );

        module.values( AccidentValue.class );
// END SNIPPET: assembler1
        module.addServices(
                ManufacturerRepositoryService.class,
                CarEntityFactoryService.class
        ).visibleIn( Visibility.application );
// START SNIPPET: assembler1
    }
}
// END SNIPPET: assembler1
// END SNIPPET: assembler2