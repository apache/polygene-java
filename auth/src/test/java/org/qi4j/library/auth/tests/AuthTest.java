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
import org.qi4j.library.auth.AuthorizationContext;
import org.qi4j.library.auth.AuthorizationContextComposite;
import org.qi4j.library.auth.AuthorizationService;
import org.qi4j.library.auth.AuthorizationServiceComposite;
import org.qi4j.library.auth.GroupComposite;
import org.qi4j.library.auth.NamedPermissionComposite;
import org.qi4j.library.auth.ProtectedResource;
import org.qi4j.library.auth.RoleAssignment;
import org.qi4j.library.auth.RoleAssignmentComposite;
import org.qi4j.library.auth.RoleComposite;
import org.qi4j.library.auth.UserComposite;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.test.Qi4jTestSetup;

public class AuthTest
    extends Qi4jTestSetup
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( UserComposite.class,
                              GroupComposite.class,
                              RoleComposite.class,
                              AuthorizationContextComposite.class,
                              NamedPermissionComposite.class,
                              RoleAssignmentComposite.class,
                              AuthorizationServiceComposite.class,
                              SecuredRoom.class );
    }

    @Test
    public void testAuth()
        throws Exception
    {
        // Create resource
        SecuredRoom room = compositeBuilderFactory.newCompositeBuilder( SecuredRoom.class ).newInstance();

        // Create user
        UserComposite user = compositeBuilderFactory.newCompositeBuilder( UserComposite.class ).newInstance();

        // Create permission
        NamedPermissionComposite permission = compositeBuilderFactory.newCompositeBuilder( NamedPermissionComposite.class ).newInstance();
        permission.name().set( "Enter room" );

        // Create role
        RoleComposite role = compositeBuilderFactory.newCompositeBuilder( RoleComposite.class ).newInstance();

        role.permissions().add( permission );

        // Create authorization service
        AuthorizationService authorization = compositeBuilderFactory.newCompositeBuilder( AuthorizationServiceComposite.class ).newInstance();

        // Create authorization context
        AuthorizationContext context = compositeBuilderFactory.newCompositeBuilder( AuthorizationContextComposite.class ).newInstance();
        context.user().set( user );
        context.time().set( new Date() );

        // Check permission
        assertFalse( authorization.hasPermission( permission, room, context ) );

        // Create role assignment
        RoleAssignmentComposite roleAssignment = compositeBuilderFactory.newCompositeBuilder( RoleAssignmentComposite.class ).newInstance();
        roleAssignment.assignee().set( user );
        roleAssignment.role().set( role );
        roleAssignment.type().set( RoleAssignment.Type.ALLOW );
        room.roleAssignments().add( roleAssignment );

        // Check permission
        assertTrue( authorization.hasPermission( permission, room, context ) );

        // Create group
        GroupComposite group = compositeBuilderFactory.newCompositeBuilder( GroupComposite.class ).newInstance();
        group.members().add( user );
        user.groups().add( group );

        // Create role assignment
        RoleAssignmentComposite groupRoleAssignment = compositeBuilderFactory.newCompositeBuilder( RoleAssignmentComposite.class ).newInstance();
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
}
