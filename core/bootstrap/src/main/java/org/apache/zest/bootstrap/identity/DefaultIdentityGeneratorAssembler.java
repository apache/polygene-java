package org.apache.zest.bootstrap.identity;

import org.apache.zest.api.identity.IdentityGenerator;
import org.apache.zest.api.identity.UuidGeneratorMixin;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;

public class DefaultIdentityGeneratorAssembler
    implements Assembler
{

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        module.services(IdentityGenerator.class).withMixins(UuidGeneratorMixin.class);
    }
}
