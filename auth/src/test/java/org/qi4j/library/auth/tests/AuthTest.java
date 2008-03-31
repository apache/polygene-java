/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.auth.tests;

import java.util.Date;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.scope.Service;
import org.qi4j.library.auth.AuthorizationContext;
import org.qi4j.library.auth.AuthorizationContextComposite;
import org.qi4j.library.auth.AuthorizationService;
import org.qi4j.library.auth.AuthorizationServiceComposite;
import org.qi4j.library.auth.Group;
import org.qi4j.library.auth.GroupEntity;
import org.qi4j.library.auth.NamedPermission;
import org.qi4j.library.auth.NamedPermissionEntity;
import org.qi4j.library.auth.ProtectedResource;
import org.qi4j.library.auth.Role;
import org.qi4j.library.auth.RoleAssignment;
import org.qi4j.library.auth.RoleAssignmentEntity;
import org.qi4j.library.auth.RoleEntity;
import org.qi4j.library.auth.User;
import org.qi4j.library.auth.UserComposite;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.test.AbstractQi4jTest;

public class AuthTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( UserComposite.class,
                              GroupEntity.class,
                              RoleEntity.class,
                              AuthorizationContextComposite.class,
                              NamedPermissionEntity.class,
                              RoleAssignmentEntity.class,
                              SecuredRoom.class );
        module.addServices( AuthorizationServiceComposite.class );
    }

    @Test
    public void testAuth()
        throws Exception
    {
        // Create resource
        SecuredRoom room = compositeBuilderFactory.newCompositeBuilder( SecuredRoom.class ).newInstance();

        // Create user
        User user = compositeBuilderFactory.newCompositeBuilder( User.class ).newInstance();

        // Create permission
        NamedPermission permission = compositeBuilderFactory.newCompositeBuilder( NamedPermission.class ).newInstance();
        permission.name().set( "Enter room" );

        // Create role
        Role role = compositeBuilderFactory.newCompositeBuilder( Role.class ).newInstance();

        role.permissions().add( permission );

        // Create authorization service
        AuthorizationService authorization = compositeBuilderFactory.newCompositeBuilder( AuthorizationService.class ).newInstance();

        // Create authorization context
        CompositeBuilder<AuthorizationContext> accb = compositeBuilderFactory.newCompositeBuilder( AuthorizationContext.class );
        accb.propertiesOfComposite().user().set( user );
        accb.propertiesOfComposite().time().set( new Date() );
        AuthorizationContext context = accb.newInstance();

        // Check permission
        assertFalse( authorization.hasPermission( permission, room, context ) );

        // Create role assignment
        RoleAssignment roleAssignment = compositeBuilderFactory.newCompositeBuilder( RoleAssignment.class ).newInstance();
        roleAssignment.assignee().set( user );
        roleAssignment.role().set( role );
        roleAssignment.type().set( RoleAssignment.Type.ALLOW );
        room.roleAssignments().add( roleAssignment );

        // Check permission
        assertTrue( authorization.hasPermission( permission, room, context ) );

        // Create group
        Group group = compositeBuilderFactory.newCompositeBuilder( Group.class ).newInstance();
        group.members().add( user );
        user.groups().add( group );

        // Create role assignment
        RoleAssignment groupRoleAssignment = compositeBuilderFactory.newComposite( RoleAssignment.class );
        groupRoleAssignment.assignee().set( group );
        groupRoleAssignment.role().set( role );
        groupRoleAssignment.type().set( RoleAssignment.Type.ALLOW );
        room.roleAssignments().add( groupRoleAssignment );

        room.roleAssignments().add( groupRoleAssignment );
        room.roleAssignments().remove( roleAssignment );

        // Check permission - user should still be allowed
        assertTrue( authorization.hasPermission( permission, room, context ) );
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface SecuredRoom
        extends Composite, ProtectedResource
    {
    }

    public class PojoRunner
    {
        @Service AuthorizationService auth;


    }
}
