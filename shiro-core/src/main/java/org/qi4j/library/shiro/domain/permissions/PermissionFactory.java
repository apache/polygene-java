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

@Mixins( PermissionFactory.Mixin.class )
public interface PermissionFactory
        extends ServiceComposite
{

    Permission create( String permissionString );

    abstract class Mixin
            implements PermissionFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        public Permission create( String permissionString )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Permission> permissionBuilder = uow.newEntityBuilder( Permission.class );
            Permission permission = permissionBuilder.instance();
            permission.string().set( permissionString );
            return permissionBuilder.newInstance();
        }

    }

}
