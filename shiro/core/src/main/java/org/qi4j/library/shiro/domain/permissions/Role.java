/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( Role.Mixin.class )
public interface Role
        extends EntityComposite
{

    Property<String> name();

    ManyAssociation<Permission> permissions();

    RoleAssignment assignTo( RoleAssignee assignee );

    abstract class Mixin
            implements Role
    {

        @This
        private Role role;
        @Structure
        private UnitOfWorkFactory uowf;

        public RoleAssignment assignTo( RoleAssignee assignee )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<RoleAssignment> roleAssignmentBuilder = uow.newEntityBuilder( RoleAssignment.class );
            RoleAssignment roleAssignment = roleAssignmentBuilder.instance();
            roleAssignment.role().set( role );
            assignee.roleAssignments().add( roleAssignment );
            roleAssignment.assignee().set( assignee );
            return roleAssignmentBuilder.newInstance();
        }

    }

}
