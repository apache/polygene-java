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
package org.qi4j.runtime.startup;

import java.io.File;
import org.junit.Test;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class SerializableApplicationTest
{
    @Test
    public void testSerializationOfSingletonAssembler()
        throws Exception
    {
        // Start first instance;
        final SingletonAssembler app1 = new SingletonAssembler( true )
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites( CarComposite.class );
                System.out.println( "Instance 1" );
            }
        };
        app1.application().passivate();

        // Start a second time. should now load from qi4j/application.qi4j

        final SingletonAssembler app2 = new SingletonAssembler( true )
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites( CarComposite.class );
                System.out.println( "Instance 2" );
            }
        };
        app2.application().passivate();

        // Cleaning up.
        new File( "qi4j/application.qi4j" ).delete();
        new File( "qi4j" ).delete();
    }


    private static interface CarComposite extends Car, Composite
    {
    }

    private static interface Car
    {
        Property<String> manufacturer();
    }
}
