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
package org.qi4j.library.shiro.domain.permissions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.shiro.domain.common.IdentifiableSubject;

@Mixins( RoleAssignee.Mixin.class )
public interface RoleAssignee
        extends IdentifiableSubject, EntityComposite
{

    @Aggregated
    @UseDefaults
    ManyAssociation<RoleAssignment> roleAssignments();

    Set<String> roleNames();

    Set<String> permissionStrings();

    public abstract class Mixin
            implements RoleAssignee
    {

        @This
        private RoleAssignee roleAssignee;

        @Override
        public Set<String> roleNames()
        {
            Set<String> roleNames = new HashSet<String>();
            for ( RoleAssignment assignment : roleAssignee.roleAssignments() ) {
                roleNames.add( assignment.role().get().name().get() );
            }
            return Collections.unmodifiableSet( roleNames );
        }

        @Override
        public Set<String> permissionStrings()
        {
            Set<String> permissionStrings = new HashSet<String>();
            for ( RoleAssignment assignment : roleAssignee.roleAssignments() ) {
                permissionStrings.addAll( assignment.role().get().permissions().get() );
            }
            return Collections.unmodifiableSet( permissionStrings );
        }

    }

}
