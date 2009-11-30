/*
 * Copyright 2007, 2008 Niclas Hedhman.
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
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.auth.AuthenticationMethod;
import org.qi4j.library.auth.AuthorizationContext;
import org.qi4j.library.auth.AuthorizationContextValue;
import org.qi4j.library.auth.AuthorizationService;
import org.qi4j.library.auth.Group;
import org.qi4j.library.auth.NamedPermission;
import org.qi4j.library.auth.ProtectedResource;
import org.qi4j.library.auth.Role;
import org.qi4j.library.auth.RoleAssignment;
import org.qi4j.library.auth.User;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class AuthTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addEntities( User.class,
                            Group.class,
                            Role.class,
                            NamedPermission.class,
                            RoleAssignment.class,
                            SecuredRoom.class );
        module.addValues( AuthorizationContextValue.class );
        module.addServices( AuthorizationService.class );

        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void testAuth()
        throws Exception
    {
        UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
        try
        {
            // Create resource
            SecuredRoom room = unit.newEntityBuilder( SecuredRoom.class ).newInstance();

            // Create user
            User user = unit.newEntityBuilder( User.class ).newInstance();

            // Create permission
            EntityBuilder<NamedPermission> entityBuilder = unit.newEntityBuilder( NamedPermission.class );
            NamedPermission permission = entityBuilder.instance();
            permission.name().set( "Enter room" );
            permission = entityBuilder.newInstance();

            // Create role
            Role role = unit.newEntityBuilder( Role.class ).newInstance();

            role.permissions().add( 0, permission );

            // Find authorization service
            AuthorizationService authorization = serviceLocator.<AuthorizationService>findService( AuthorizationService.class )
                .get();

            // Create authorization context
            ValueBuilder<AuthorizationContext> accb = valueBuilderFactory.newValueBuilder( AuthorizationContext.class );
            AuthorizationContext context = accb.prototype();
            context.user().set( user );
            context.time().set( new Date() );
            context.authenticationMethod().set( AuthenticationMethod.BASIC );
            context = accb.newInstance();

            // Check permission
            assertFalse( authorization.hasPermission( permission, room, context ) );

            // Create role assignment
            EntityBuilder<RoleAssignment> roleAssignmentEntityBuilder = unit.newEntityBuilder( RoleAssignment.class );
            RoleAssignment roleAssignment = roleAssignmentEntityBuilder.instance();
            roleAssignment.assignee().set( user );
            roleAssignment.role().set( role );
            roleAssignment.roleType().set( RoleAssignment.RoleType.ALLOW );
            roleAssignment = roleAssignmentEntityBuilder.newInstance();
            room.roleAssignments().add( 0, roleAssignment );

            // Check permission
            assertTrue( authorization.hasPermission( permission, room, context ) );

            // Create group
            Group group = unit.newEntityBuilder( Group.class ).newInstance();
            group.members().add( 0, user );
            user.groups().add( 0, group );

            // Create role assignment
            EntityBuilder<RoleAssignment> assignmentEntityBuilder = unit.newEntityBuilder( RoleAssignment.class );
            RoleAssignment groupRoleAssignment = assignmentEntityBuilder.instance();
            groupRoleAssignment.assignee().set( group );
            groupRoleAssignment.role().set( role );
            groupRoleAssignment.roleType().set( RoleAssignment.RoleType.ALLOW );
            groupRoleAssignment = assignmentEntityBuilder.newInstance();

            room.roleAssignments().add( 0, groupRoleAssignment );

            room.roleAssignments().add( 0, groupRoleAssignment );
            room.roleAssignments().remove( roleAssignment );

            // Check permission - user should still be allowed
            assertTrue( authorization.hasPermission( permission, room, context ) );
        }
        finally
        {
            unit.discard();
        }
    }

    public interface SecuredRoom
        extends EntityComposite, ProtectedResource
    {
    }

    public class PojoRunner
    {
        @Service
        AuthorizationService auth;
    }
}
