/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import org.junit.Test;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * @author Stanislav Muhametsin
 */
public class QI273Test
    extends AbstractQi4jTest
{

    public static interface RoleA
    {
        public Property<String> theProperty();
    }

    public static interface RoleB
    {
        public Property<String> theProperty();
    }

    @Mixins( SomeDomainEntityMixin.class )
    public static interface SomeDomainEntity
        extends EntityComposite
    {
        public String getRoleAProperty();

        public String getRoleBProperty();
    }

    public static abstract class SomeDomainEntityMixin
        implements SomeDomainEntity
    {
        @This
        private RoleA _a;
        @This
        private RoleB _b;

        public String getRoleAProperty()
        {
            return this._a.theProperty().get();
        }

        public String getRoleBProperty()
        {
            return this._b.theProperty().get();
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( SomeDomainEntity.class );
    }

    @Test
    public void doTest()
        throws Exception
    {
/*
        UnitOfWork uow = this.module.newUnitOfWork();

        EntityBuilder<SomeDomainEntity> builder = uow.newEntityBuilder( SomeDomainEntity.class );
        builder.instanceFor( RoleA.class ).theProperty().set( "a" );
        builder.instanceFor( RoleB.class ).theProperty().set( "b" );
        SomeDomainEntity entity = builder.newInstance();

        Assert.assertEquals( "Property must be same as set.", "a", entity.getRoleAProperty() );
        Assert.assertEquals( "Property must be same as set.", "b", entity.getRoleBProperty() );

        uow.complete();
*/
    }
}
