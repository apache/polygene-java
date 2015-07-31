/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.objects;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Unit tests for ObjectBuilderFactory.
 */
public class ObjectBuilderFactoryTest
{

    /**
     * Tests that an object builder cannot be created for an unregistered object.
     *
     * @throws Exception expected
     */
    @Test( expected = NoSuchObjectException.class )
    public void newBuilderForUnregisteredObject()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
            }
        };
        assembler.module().newObject( AnyObject.class );
    }

    /**
     * Tests that an object builder cannot be created for a 'null' type.
     *
     * @throws Exception expected
     */
    @Test( expected = NullArgumentException.class )
    public void newBuilderForNullType()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
            }
        };
        assembler.module().newObject( null );
    }

    /**
     * Tests that an object builder cannot be created for a 'null' type.
     *
     * @throws Exception expected
     */
    @Test( expected = NullArgumentException.class )
    public void newObjectInstanceForNullType()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
            }
        };
        assembler.module().newObject( null );
    }

    /**
     * Tests that an object can be created for an registered object class.
     */
    @Test
    public void newInstanceForRegisteredObject()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( AnyObject.class );
            }
        };
        assembler.module().newObject( AnyObject.class );
    }

    @Test
    public void givenManyConstructorsWhenInstantiateThenChooseCorrectConstructor()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( ManyConstructorObject.class );
            }
        };

        ManyConstructorObject object = assembler.module().newObject( ManyConstructorObject.class );
        Assert.assertThat( "ref is not null", object.anyObject, notNullValue() );

        object = assembler.module()
            .newObject( ManyConstructorObject.class, new AnyObject() );

        Assert.assertThat( "ref is not null", object.anyObject, notNullValue() );
    }

    @Test
    public void givenClassWithInnerClassesWhenInstantiateThenInstantiateInnerClass()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( OuterClass.class );
            }
        };

        Assert.assertThat( "inner class has been injected", assembler.module()
            .newObject( OuterClass.class )
            .name(), equalTo( "Module 1" ) );
    }

    public static final class AnyObject
    {
    }

    public static final class ManyConstructorObject
    {
        AnyObject anyObject;
        Module module;

        public ManyConstructorObject( @Uses AnyObject anyObject, @Structure Module module )
        {
            this.anyObject = anyObject;
            this.module = module;
        }

        public ManyConstructorObject( @Structure Module module )
        {
            this.module = module;
        }
    }
}
