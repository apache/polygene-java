/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

/**
 * TODO
 */
public class AuthorizationServiceMixin implements AuthorizationService
{
    public boolean hasPermission( Permission requiredPermission, HasRoleAssignments resource, AuthorizationContext context )
    {
        for( RoleAssignment roleAssignment : resource.roleAssignments() )
        {
            if( roleAssignment.assignee().get() instanceof HasMembers )
            {
                // Check if user is a member of this group or not
                if( !context.user().get().groups().contains( roleAssignment.assignee().get() ) )
                {
                    continue; // Not a member - check next assignment
                }

            }
            else if( !roleAssignment.assignee().equals( context.user() ) )
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
                    if( roleAssignment.type().equals( RoleAssignment.Type.ALLOW ) )
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
