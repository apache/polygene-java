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
import org.qi4j.library.shiro.domain.permissions.Role;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.qi4j.library.shiro.domain.permissions.RoleAssignment;
import org.qi4j.library.shiro.domain.permissions.RoleFactory;

public class PermissionsDomainAssembler
        implements Assembler
{

    private Visibility visibility = Visibility.module;

    public PermissionsDomainAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.entities( Role.class, RoleAssignment.class, RoleAssignee.class ).visibleIn( visibility );
        module.services( RoleFactory.class ).visibleIn( visibility );
    }

}
