/*
 * Copyright (c) 2007-2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007-2008, Niclas Hedhman. All Rights Reserved.
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

package org.qi4j.library.auth;

import org.qi4j.api.entity.association.ManyAssociation;

/**
 * JAVADOC
 */
public abstract class AuthorizationMixin
    implements AuthorizationService
{
    public boolean hasPermission( Permission requiredPermission, ProtectedResource resource, AuthorizationContext context )
    {
        User user = context.user().get();
        ManyAssociation<Members> members = user.groups();
        for( RoleAssignment roleAssignment : resource.roleAssignments() )
        {
            RoleAssignee assignee = roleAssignment.assignee().get();
            if( assignee instanceof Members )
            {
                // Check if user is a member of this group or not
                if( !members.contains( (Members) assignee ) )
                {
                    continue; // Not a member - check next assignment
                }

            }
            else if( !assignee.equals( user ) )
            {
                continue; // Wrong user - check next assignment
            }

            // This assignment could be valid

            // Check condition
            RoleCondition condition = roleAssignment.condition().get();
            if( condition != null && !condition.isValid( context ) )
            {
                continue;
            }

            for( Permission permission : roleAssignment.role().get().permissions() )
            {
                if( permission.equals( requiredPermission ) )
                {
                    return roleAssignment.roleType().get().equals( RoleAssignment.RoleType.ALLOW );
                }
            }
        }
        return false;
    }
}
