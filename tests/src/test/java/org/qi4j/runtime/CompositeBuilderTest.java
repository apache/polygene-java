/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.ConcernOf;
import org.qi4j.composite.Concerns;
import org.qi4j.entity.Lifecycle;
import org.qi4j.test.AbstractQi4jTest;

public class CompositeBuilderTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( Model1.class );
    }

    @Test
    public void testNewInstance()
        throws Exception
    {
        CompositeBuilder<Model1> impl = compositeBuilderFactory.newCompositeBuilder( Model1.class );
        impl.newInstance();
        assertEquals( true, Model1LifecycleConcern.createMethod );
    }

    @Concerns( Model1LifecycleConcern.class )
    public static interface Model1 extends Composite, Lifecycle
    {
    }

    public static class Model1LifecycleConcern extends ConcernOf<Lifecycle>
        implements Lifecycle
    {
        static boolean deleteMethod;
        static boolean createMethod;

        /**
         * Creation callback method.
         * <p/>
         * Called by the Qi4J runtime before the newInstance of the composite completes, allowing
         * for additional initialization.
         */
        public void create()
        {
            createMethod = true;
            next.create();
        }

        /**
         * Deletion callback method.
         * <p/>
         * Called by the Qi4J runtime before the composite is deleted from the system, allowing
         * for clean-up operations.
         */
        public void remove()
        {
            deleteMethod = true;
            next.remove();
        }
    }
}
