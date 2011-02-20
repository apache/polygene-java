package org.qi4j.library.struts2.codebehind.bootstrap;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.struts2.codebehind.Qi4jCodebehindPackageProvider;

public class CodebehindAssembler
    implements Assembler
{

    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addObjects( Qi4jCodebehindPackageProvider.class );
    }
}
