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
package org.apache.polygene.library.shiro.domain.permissions;

import java.util.List;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;

@Mixins( Role.Mixin.class )
public interface Role
    extends EntityComposite
{

    Property<String> name();

    @UseDefaults
    Property<List<String>> permissions();

    RoleAssignment assignTo( RoleAssignee assignee );

    abstract class Mixin
        implements Role
    {

        @Structure
        private UnitOfWorkFactory uowf;

        @This
        private Role role;

        @Override
        public RoleAssignment assignTo( RoleAssignee assignee )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
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
