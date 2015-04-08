
package @@ROOT_PACKAGE@@.boot;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;

@@IMPORTS@@

public class @@LAYER_NAME@@Assembler
{
    public @@LAYER_NAME@@Assembler()
    {}

    public LayerAssembly assemble( ApplicationAssembly assembly )
        throws AssemblyException
    {
@@MODULE_CREATION@@
    }

@@MODULE_CREATE_METHODS@@
}
