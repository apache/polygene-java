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

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.entities( Role.class, RoleAssignment.class, RoleAssignee.class ).visibleIn( visibility );
        module.services( RoleFactory.class ).visibleIn( visibility );
    }

}
