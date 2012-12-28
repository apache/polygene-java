package org.qi4j.library.struts2.codebehind.bootstrap;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.struts2.codebehind.Qi4jCodebehindPackageProvider;

public class CodebehindAssembler
    implements Assembler
{

    @Override
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.objects( Qi4jCodebehindPackageProvider.class );
    }
}
