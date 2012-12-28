/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.tests.regression.niclas2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;


public class ConcernsOnPropertyTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( CarComposite.class );
    }


    @Test
    public void whenConcernOnPropertyThenConcernIsInvoked()
    {
        TransientBuilder<Car> builder = module.newTransientBuilder( Car.class );
        Car prototype = builder.prototypeFor( Car.class );
        prototype.manufacturer().set( "Volvo" );
        Car car = builder.newInstance();
        Assert.assertEquals( "Concern on Property methods.", "Simon says: Volvo", car.manufacturer().get() );
    }

    public interface CarComposite
        extends Car, TransientComposite
    {
    }

    public interface Car
    {
        Manufacturer manufacturer();
    }

    @SideEffects( SystemOutSideEffect.class )
    @Concerns( SimonSays.class )
    public interface Manufacturer
        extends Property<String>
    {
    }

    public class SimonSays
        extends ConcernOf<InvocationHandler>
        implements InvocationHandler
    {

        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            Object result = next.invoke( o, method, objects );
            if( result instanceof String )
            {
                return "Simon says: " + result;
            }
            return result;
        }
    }

    public class SystemOutSideEffect
        extends SideEffectOf<InvocationHandler>
        implements InvocationHandler
    {

        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            try
            {
                Object result = this.result.invoke( o, method, objects );
                if( result instanceof String )
                {
                    System.out.println( "[INFO] " + result );
                }
                return result;
            }
            catch( Exception e )
            {
                e.printStackTrace( System.out );
                throw e;
            }
        }
    }
}
