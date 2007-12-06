/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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

package org.qi4j.library.framework.rdf;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.SideEffects;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.composite.scope.SideEffectFor;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ApplicationXmlTest
    extends AbstractQi4jTest
{

    @Override public void configure( ModuleAssembly module )
    {
        module.getLayerAssembly().getApplicationAssembly().setName( "testapp" );
        module.getLayerAssembly().setName( "testlayer" );
        module.setName( "testmodule" );
        module.addComposite( TestComposite.class );
    }

    public void testApplicationXml()
        throws Exception
    {
        ApplicationRdfXml applicationRdfXml = new ApplicationRdfXml( application.getApplicationContext() );

        File file = new File( "libraries/framework/target/classes/application.xml" );
        FileWriter fileWriter = new FileWriter( file );
        PrintWriter out = new PrintWriter( fileWriter );
        applicationRdfXml.print( out );
        fileWriter.close();
        System.out.println( "RDF/XML written to " + file.getAbsolutePath() );
    }

    @Mixins( AMixin.class )
    public interface A
    {
        String doStuff();
    }

    public static class AMixin
        implements A
    {
        @ThisCompositeAs B bRef;

        public String doStuff()
        {
            return bRef.otherStuff() + "123";
        }
    }

    public interface B
    {
        String otherStuff();
    }

    public static class BMixin
        implements B
    {
        public String otherStuff()
        {
            return "XYZ";
        }
    }

    public static class OtherStuffConcern
        implements B
    {
        @ConcernFor B next;

        public String otherStuff()
        {
            return next.otherStuff() + "!";
        }
    }

    public static class LogSideEffect
        implements InvocationHandler
    {
        @SideEffectFor InvocationHandler next;

        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            System.out.println( "Called " + method.getName() );
            return null;
        }
    }

    @SideEffects( LogSideEffect.class )
    @Concerns( OtherStuffConcern.class )
    @Mixins( { BMixin.class } )
    public interface TestComposite
        extends A, Composite
    {
    }
}
