/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.test.context;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.context.Context;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;
import org.qi4j.test.AbstractQi4jTest;

public class ContextCompositeTest extends AbstractQi4jTest
{

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testThreadScope()
        throws InterruptedException
    {
        CompositeBuilder<MyContextComposite> builder = compositeBuilderFactory.newCompositeBuilder( MyContextComposite.class );
        builder.propertiesFor( MyData.class ).data().set( 0 );
        MyContextComposite c1 = builder.newInstance();
        Worker w1 = new Worker( c1, 100 );
        Worker w2 = new Worker( c1, 200 );
        w1.start();
        w2.start();
        w1.join();
        w2.join();
        Thread.sleep( 200 );
        assertEquals( 100, w1.getData() );
        assertEquals( 200, w2.getData() );
    }

    public void configure( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( MyContextComposite.class );
    }

    static interface MyContextComposite extends Context, Composite, MyData
    {
    }

    @Mixins( PropertyMixin.class )
    static interface MyData
    {
        Property<Integer> data();
    }

    static class Worker extends Thread
    {
        private MyContextComposite composite;
        private int loops;
        private int data;


        public Worker( MyContextComposite composite, int loops )
        {
            this.composite = composite;
            this.loops = loops;
        }

        public void run()
        {
            try
            {
                for( int i = 0; i < loops; i++ )
                {
                    int value = composite.data().get();
                    value = value + 1;
                    composite.data().set( value );
                    Thread.sleep( 1 );
                }
            }
            catch( InterruptedException e )
            {
                e.printStackTrace();
            }
            data = composite.data().get();
        }

        public int getData()
        {
            return data;
        }
    }
}
