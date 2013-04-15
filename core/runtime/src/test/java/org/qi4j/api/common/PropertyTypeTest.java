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

package org.qi4j.api.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * Test for ability to set constraints on Properties
 */
public class PropertyTypeTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
        module.transients( PersonComposite.class );
    }

    @Test
    public void givenEntityWithPropertyConstraintsWhenInstantiatedThenPropertiesWork()
        throws Exception
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder = unitOfWork.newEntityBuilder( PersonEntity.class );
            PersonEntity personEntity = builder.instance();
            personEntity.givenName().set( "Rickard" );
            personEntity.familyName().set( "Öberg" );
            personEntity = builder.newInstance();

            personEntity.givenName().set( "Niclas" );
            personEntity.familyName().set( "Hedhman" );

            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void givenCompositeWithPropertyConstraintsWhenInstantiatedThenPropertiesWork()
        throws Exception
    {
        TransientBuilder<PersonComposite> builder = module.newTransientBuilder( PersonComposite.class );
        PersonComposite personComposite = builder.prototype();
        personComposite.givenName().set( "Rickard" );
        personComposite.familyName().set( "Öberg" );
        personComposite = builder.newInstance();

        personComposite.givenName().set( "Niclas" );
        personComposite.familyName().set( "Hedhman" );
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @MaxLength( 50 )
    public @interface Name
    {
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @NotEmpty
    @Name
    public @interface GivenName
    {
    }

    interface PersonEntity
        extends EntityComposite
    {
        @GivenName
        Property<String> givenName();

        @Name
        Property<String> familyName();
    }

    interface PersonComposite
        extends TransientComposite
    {
        @GivenName
        Property<String> givenName();

        @Name
        Property<String> familyName();
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @Constraints( MaxLengthConstraint.class )
    public @interface MaxLength
    {
        int value();
    }

    public static class MaxLengthConstraint
        implements Constraint<MaxLength, String>
    {
        public boolean isValid( MaxLength annotation, String argument )
        {
            if( argument != null )
            {
                return argument.length() <= annotation.value();
            }

            return false;
        }
    }

    @ConstraintDeclaration
    @Retention( RetentionPolicy.RUNTIME )
    @Constraints( { NotEmptyStringConstraint.class } )
    public @interface NotEmpty
    {
    }

    public static class NotEmptyStringConstraint
        implements Constraint<NotEmpty, String>
    {

        public boolean isValid( NotEmpty annotation, String value )
        {
            return value.trim().length() > 0;
        }
    }
}
