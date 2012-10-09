/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
import org.qi4j.library.shiro.domain.passwords.PasswordRealmConfiguration;
import org.qi4j.library.shiro.domain.passwords.PasswordRealmService;
import org.qi4j.library.shiro.domain.passwords.PasswordSecurable;

public class PasswordDomainAssembler
        implements Assembler
{

    private Visibility visibility = Visibility.layer;

    private ModuleAssembly configModule;

    private Visibility configVisibility = Visibility.layer;

    public PasswordDomainAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public PasswordDomainAssembler withConfig( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return this;
    }

    public PasswordDomainAssembler withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.entities( PasswordSecurable.class ).visibleIn( visibility );
        module.services( PasswordRealmService.class ).instantiateOnStartup().visibleIn( visibility );
        if ( configModule == null ) {
            configModule = module;
        }
        configModule.entities( PasswordRealmConfiguration.class ).visibleIn( configVisibility );
    }

}
