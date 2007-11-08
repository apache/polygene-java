/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.structure;

import java.util.Iterator;
import org.qi4j.Composite;
import org.qi4j.annotation.Mixins;
import org.qi4j.annotation.scope.Service;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class AssemblyTest
    extends AbstractQi4jTest
{
    public void testAssembly()
    {
        api.getAssemblyRegistry().addAssembly( new TestAssembly() );

        assertEquals( Model1Composite.class, api.getCompositeRegistry().getCompositeType( Model1.class ) );

        Iterator<Model1Composite> prototype = factory.newCompositeBuilder( Model1Composite.class ).iterator();
        Model1 instance = prototype.next();

        String result = instance.prefixThings( "Simon sez:" );
        assertEquals( "Simon sez:Hello World", result );
    }

    class TestAssembly
        extends AbstractAssembly
    {
        @Override public void configure( CompositeMapper mapper )
        {
            mapper.register( Model1Composite.class, Model1.class );
        }

        @Override public void configure( DependencyBinder binder )
        {
            BindWhere where;
            BindHow how;
            BindWhat what;

/*
            binder.bind( where.interfaceIs(MyService.class),
                         how.singleton(),
                         what.instanceOf(MyServiceImpl.class));
            binder.bind( where.interfaceIs(MyService.class),
                         how.singleton(),
                         what.instance(new MyServiceImpl()));
            binder.bind( where.interfaceIs(MyService.class).nameIs("Foo"),
                         how.prototype(),
                         what.instanceOf(Model1Composite.class));
            binder.bind( where.interfaceIs(MyService.class),
                         how.singleton(),
                         what.iterator(compositeBuilder));

            binder.bind(how.singleton(where.interfaceIs(MyService.class),
                                      what.instanceOf(ServiceImpl.class)));
*/
        }

        protected void doConfigure()
        {
            // <how>(<where>, <what>);
/*

            singleton(interfaceIs(MyService.class),
                      instanceOf(MyServiceImpl.class));

            singleton(interfaceIs(MyService.class),
                      instance(new MyServiceImpl()));

            prototype(interfaceIs(MyService.class),
                      instanceOf(Model1Composite.class));

            prototype(interfaceIs(MyService.class),
                      iterator(compositeBuilder));

            singleton(interfaceIs(MyService.class).nameIs("SomeService"),
                      instanceOf(ServiceImpl.class));

            singleton(interfaceIs(MyService.class).annotatedWith(Service.class),
                      instanceOf(ServiceImpl.class));

            singleton(interfaceIs(MyService.class).annotatedWith(Service.class),
                      decorate(instanceOf(CachingComposite.class),
                               instanceOf(ServiceImpl.class)));

            singleton(interfaceIs(MyService.class).annotatedWith(Service.class),
                      decorate(instanceOf(CachingComposite.class),
                               instanceOf(FailoverComposite.class),
                               jndiLookup("MyService")));

            singleton(interfaceIs(MyService.class).annotatedWith(Service.class),
                      jndiLookup("foobar"));

            DependencyKey key = interfaceIs(MyService.class);
            singleton(key.nameIs("Service1"), instanceOf(MyServiceImpl));
            singleton(key.dependentType(Model1Mixin.class), instanceOf(MyServiceImpl));
            singleton(key.dependentType(Model2Mixin.class), instanceOf(MyServiceImpl));
            
*/
        }


        @Override public void configure( QueryMapper mapper )
        {
            super.configure( mapper );
        }
    }

    public static interface Model1
    {
        String prefixThings( String aString );
    }

    public static class Model1Mixin
        implements Model1
    {
        @Service MyService service;

        public String prefixThings( String aString )
        {
            return aString + service.getThings();
        }
    }

    public interface MyService
    {
        String getThings();
    }

    public static class MyServiceImpl
        implements MyService
    {
        public String getThings()
        {
            return "Hello World";
        }
    }

    @Mixins( MyServiceImpl.class )
    public static interface MyServiceComposite
        extends Composite, MyService
    {
    }

    @Mixins( Model1Mixin.class )
    public static interface Model1Composite
        extends Composite, Model1
    {
    }
}