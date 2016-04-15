/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.shiro.domain.permissions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.Aggregated;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.library.shiro.domain.common.IdentifiableSubject;

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
