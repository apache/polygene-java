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

package org.qi4j.runtime.transients;

import java.lang.reflect.Method;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.NoSuchTransientException;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.constraints.annotation.MaxLength;

import static org.junit.Assert.assertThat;

/**
 * Unit tests for CompositeBuilderFactory.
 */
public class TransientBuilderFactoryTest
{

    /**
     * Tests that an transient builder cannot be created for an unregistered object.
     *
     * @throws Exception expected
     */
    @Test( expected = NoSuchTransientException.class )
    public void newBuilderForUnregisteredComposite()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
            }
        };
        assembler.module().newTransientBuilder( AnyComposite.class );
    }

    /**
     * Tests that an transient builder cannot be created for a 'null' type.
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
        assembler.module().newTransientBuilder( null );
    }

    /**
     * Tests that a transient composite instance cannot be created for a 'null' type.
     *
     * @throws Exception expected
     */
    @Test( expected = NullArgumentException.class )
    public void newInstanceForNullType()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
            }
        };
        assembler.module().newTransient( null );
    }

    /**
     * Tests that an object builder can be created for an registered object.
     */
    @Test
    public void newBuilderForRegisteredComposite()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( AnyComposite.class );
            }
        };
        assembler.module().newTransientBuilder( AnyComposite.class );
    }

    /**
     * Tests that an object can be created for an registered object class.
     */
    @Test
    public void newInstanceForRegisteredComposite()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( AnyComposite.class );
            }
        };
        assembler.module().newTransientBuilder( AnyComposite.class );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testClassAsTransient()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( AnyTransient.class );
            }
        };

        AnyTransient anyTransient = assembler.module().newTransient( AnyTransient.class );
        assertThat( anyTransient.hello( "me" ), new IsEqual<String>( "Hello ME from Module 1" ) );

        assertThat( anyTransient.hello( "World" ), new IsEqual<String>( "Hello WORLD from ME" ) );
        anyTransient.hello( "Universe" );
    }

    public static interface AnyComposite
        extends TransientComposite
    {
    }

    public static class CapitalizeConcern
        extends GenericConcern
    {
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            if( args != null )
            {
                args[ 0 ] = ( (String) args[ 0 ] ).toUpperCase();
                return next.invoke( proxy, method, args );
            }
            else
            {
                return next.invoke( proxy, method, args );
            }
        }
    }

    @Concerns( CapitalizeConcern.class )
    public static class AnyTransient
        implements TransientComposite
    {
        @Structure
        Module module;

        public String hello( @MaxLength( 5 ) String name )
        {
            try
            {
                String from = data.foo().get();
                if( from.length() == 0 )
                {
                    from = module.name();
                }
                return "Hello " + name + " from " + from;
            }
            finally
            {
                data.foo().set( name );
            }
        }

        @This
        AnyData data;
    }

    interface AnyData
    {
        @UseDefaults
        Property<String> foo();
    }
}