/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;
import org.junit.Test;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.NoSuchTransientException;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.fail;

public class CompositeFactoryImplTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // This is required to instantiate [SecondComposite] composite in [testNewComposition9]
        module.transients( SecondComposite.class );
    }

    @SuppressWarnings( "unchecked" )
    @Test( expected = NoSuchTransientException.class )
    public void testNewInstanceNotExtendingComposite()
        throws Exception
    {
        Class aClass = FirstComposite.class;
        TransientBuilder builder = module.newTransientBuilder( aClass );
        builder.newInstance();
    }

    @Test
    public void testNewComposition9()
        throws Exception
    {
        try
        {
            TransientBuilder<SecondComposite> builder = module.newTransientBuilder(
                SecondComposite.class );
            SecondComposite composition9 = builder.newInstance();
            composition9.setValue( "satisfiedBy value" );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Fail to instantiate composite: " + SecondComposite.class );
        }
    }

    @Mixins( PropertiesMixin.class )
    public interface FirstComposite
        extends Mixin3
    {
    }

    @Mixins( PropertiesMixin.class )
    public interface SecondComposite
        extends Mixin3, TransientComposite
    {
    }

    public interface Mixin3
    {
        void setValue( String value );

        String getValue();
    }

    /**
     * Generic property mixin. Methods in interface
     * can be of the following types:
     * setFoo = set property named foo
     * getFoo = get property named foo
     * addFoo = add object to list named foo
     * removeFoo = remove object from list named foo
     * fooIterator - return an iterator over the list of Foos
     */
    @AppliesTo( { Getters.class, Setters.class } )
    public static class PropertiesMixin
        implements InvocationHandler
    {
        private Properties properties;

        public PropertiesMixin()
        {
            properties = new Properties();
        }

        @SuppressWarnings( "unchecked" )
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            String methodName = method.getName();
            String property = methodName.substring( 3 );
            if( methodName.startsWith( "get" ) )
            {
                return properties.get( property );
            }
            else
            {
                properties.put( property, args[ 0 ] );
                return null;
            }
        }
    }

    /**
     * Filter for getter methods. Method name must match "get*" or "is*" or "has*".
     */
    public static class Getters
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
        {
            final String name = method.getName();
            return !method.getReturnType().equals( Void.TYPE ) && name.startsWith( "get" ) && name.length() > 4 &&
                   method.getParameterTypes().length == 0;
        }
    }

    /**
     * Filter for setter methods. Method name must match "set*","add*" or "remove*".
     */
    public static class Setters
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
        {
            final String name = method.getName();
            return method.getReturnType().equals( Void.TYPE ) && name.startsWith( "set" ) && name.length() > 4 &&
                   method.getParameterTypes().length == 1;
        }
    }
}
