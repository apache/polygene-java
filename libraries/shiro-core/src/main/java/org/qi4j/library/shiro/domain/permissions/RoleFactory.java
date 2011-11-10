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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( RoleFactory.Mixin.class )
public interface RoleFactory
        extends ServiceComposite
{

    Role create( String name, Iterable<Permission> permissions );

    abstract class Mixin
            implements RoleFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        public Role create( String name, Iterable<Permission> permissions )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Role> roleBuilder = uow.newEntityBuilder( Role.class );
            Role role = roleBuilder.instance();
            role.name().set( name );
            for ( Permission eachPermission : permissions ) {
                role.permissions().add( eachPermission );
            }
            return roleBuilder.newInstance();
        }

    }

}
