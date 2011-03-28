/*
 * Copyright 2008 Sonny Gill. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.constraints.qi73;

import org.junit.After;
import org.junit.Test;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.constraints.annotation.NotEmpty;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class IssueTest
    extends AbstractQi4jTest
{

    private UnitOfWork unitOfWork;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( Owner.class );
    }

    @After
    public void tearDown()
        throws Exception
    {
        if( unitOfWork != null )
        {
            unitOfWork.complete();
        }
    }

    @Test
    public void testConstraintCanBeAppliedOnComputedProperty()
        throws UnitOfWorkCompletionException
    {
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<Owner> builder = unitOfWork.newEntityBuilder( Owner.class );
        builder.instance().firstName().set( "A" );
        builder.instance().lastName().set( "B" );
        builder.newInstance();
    }

    interface HasName
    {
        @NotEmpty
        @Computed
        Property<String> name();
    }

    @Mixins( NameMixin.class )
    interface Owner
        extends HasName, EntityComposite
    {
        Property<String> firstName();

        Property<String> lastName();
    }

    public static class NameMixin
        implements HasName, Initializable
    {
        @This
        Owner owner;
        @State
        Property<String> name;

        public void initialize()
            throws ConstructionException
        {
            name = new ComputedPropertyInstance<String>( name )
            {
                public String get()
                {
                    return owner.firstName().get() + " " + owner.lastName().get();
                }
            };
        }

        public Property<String> name()
        {
            return name;
        }
    }
}
