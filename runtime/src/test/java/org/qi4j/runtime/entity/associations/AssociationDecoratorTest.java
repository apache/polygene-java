/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity.associations;

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Tests for decoration of associations
 */
public class AssociationDecoratorTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( Employees.class, Boss.class );
        module.addEntities( CompanyEntity.class, Person.class );
        module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
    }

    @Test
    public void testAssociationDecorator()
        throws Exception
    {

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {

            Person employee;
            {
                EntityBuilder<Person> builder = unitOfWork.newEntityBuilder( Person.class );
                employee = builder.instance();
                employee.name().set( "Rickard" );
                employee = builder.newInstance();
            }

            CompanyEntity companyEntity;
            {
                EntityBuilder<CompanyEntity> builder = unitOfWork.newEntityBuilder( CompanyEntity.class );

                companyEntity = builder.newInstance();
            }
            companyEntity.employees().add( 0, employee );
            companyEntity.employees().remove( employee );

            companyEntity.boss().set( employee );

            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
            throw e;
        }
    }

    public interface CompanyEntity
        extends EntityComposite
    {
        Employees employees();

        @Optional
        Boss boss();
    }

    public interface Person
        extends EntityComposite
    {
        Property<String> name();
    }

    @Mixins( DecoratorMixin.class )
    @Concerns( EmployeesAuditConcern.class )
    public interface Employees
        extends ManyAssociation<Person>, TransientComposite
    {
    }

    @Mixins( DecoratorMixin.class )
    @Concerns( BossAuditSideEffect.class )
    public interface Boss
        extends Association<Person>, TransientComposite
    {
    }

    public static abstract class EmployeesAuditConcern
        extends ConcernOf<Employees>
        implements Employees
    {
        public boolean add( int i, Person entity )
        {
            System.out.println( "Added employee " + entity.name() );
            return next.add( i, entity );
        }

        public boolean remove( Person entity )
        {
            System.out.println( "Removed employee " + entity.name() );
            return next.remove( entity );
        }
    }

    public static abstract class BossAuditSideEffect
        extends SideEffectOf<Employees>
        implements Boss
    {
        public void set( Person boss )
            throws IllegalArgumentException
        {
            System.out.println( boss.name() + " is the new boss" );
        }
    }
}

