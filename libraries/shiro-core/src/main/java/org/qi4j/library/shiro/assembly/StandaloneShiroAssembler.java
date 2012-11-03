/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

    private Visibility visibility = Visibility.module;

    private ModuleAssembly configModule;

    private Visibility configVisibility = Visibility.layer;

    public StandaloneShiroAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

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

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( IniSecurityManagerService.class ).
                visibleIn( visibility ).
                instantiateOnStartup();
        if ( configModule == null ) {
            configModule = module;
        }
        configModule.entities( ShiroIniConfiguration.class ).
                visibleIn( configVisibility );
    }

}
