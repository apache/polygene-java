package org.qi4j.library.shiro.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.ini.IniSecurityManagerService;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;

public class StandaloneShiroAssembler
        implements Assembler
{

    private ModuleAssembly configModule;

    private Visibility configVisibility = Visibility.layer;

    public StandaloneShiroAssembler withConfig( ModuleAssembly config )
    {
        this.configModule = config;
        return this;
    }

    public StandaloneShiroAssembler withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return this;
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( IniSecurityManagerService.class ).
                visibleIn( Visibility.module ).
                instantiateOnStartup();
        if ( configModule == null ) {
            configModule = module;
        }
        configModule.entities( ShiroIniConfiguration.class ).
                visibleIn( configVisibility );
    }

}
