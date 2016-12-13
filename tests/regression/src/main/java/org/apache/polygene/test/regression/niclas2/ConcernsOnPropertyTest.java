/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.test.regression.niclas2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.sideeffect.SideEffectOf;
import org.apache.polygene.api.sideeffect.SideEffects;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;


public class ConcernsOnPropertyTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( CarComposite.class );
    }

    @Test
    public void whenConcernOnPropertyThenConcernIsInvoked()
    {
        TransientBuilder<Car> builder = transientBuilderFactory.newTransientBuilder( Car.class );
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
