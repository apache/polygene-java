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
package org.apache.zest.library.shiro.domain.permissions;

import java.util.List;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;

@Mixins( Role.Mixin.class )
public interface Role
        extends EntityComposite
{

    Property<String> name();

    @UseDefaults
    Property<List<String>> permissions();

    RoleAssignment assignTo( RoleAssignee assignee );

    public abstract class Mixin
            implements Role
    {

        @Structure
        private Module module;

        @This
        private Role role;

        @Override
        public RoleAssignment assignTo( RoleAssignee assignee )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<RoleAssignment> builder = uow.newEntityBuilder( RoleAssignment.class );
            RoleAssignment assignment = builder.instance();
            assignment.assignee().set( assignee );
            assignment.role().set( role );
            assignment = builder.newInstance();
            assignee.roleAssignments().add( assignment );
            return assignment;
        }

    }

}
