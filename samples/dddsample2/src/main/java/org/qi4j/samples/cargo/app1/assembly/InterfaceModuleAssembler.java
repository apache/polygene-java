package org.qi4j.samples.cargo.app1.assembly;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.cxf.CxfAssembler;
import org.qi4j.samples.cargo.app1.ui.booking.BookingServiceFacade;

public class InterfaceModuleAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( BookingServiceFacade.class ).instantiateOnStartup();
        new CxfAssembler().assemble( module );
    }
}
