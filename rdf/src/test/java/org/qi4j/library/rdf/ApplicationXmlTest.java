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

package org.qi4j.library.rdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.n3.N3WriterFactory;
import org.openrdf.rio.rdfxml.RDFXMLWriterFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConcernOf;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.SideEffectOf;
import org.qi4j.composite.SideEffects;
import org.qi4j.library.rdf.parse.StructureParser;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ApplicationXmlTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        LayerAssembly layerAssembly = module.getLayerAssembly();
        layerAssembly.getApplicationAssembly().setName( "testapp" );
        layerAssembly.setName( "testlayer" );
        module.setName( "testmodule" );
        module.addComposites( TestComposite.class );
    }

    @Test
    public void testApplicationXml()
        throws Exception
    {
        String name = "application";
        StructureParser parser = new StructureParser();
        Graph graph = parser.parse( null, "urn:qi4j:dev/tests/application" ); // TODO Fix this
        writeN3( graph, name );
        writeXml( graph, name );
    }

    private void writeN3( Graph graph, String name )
        throws RDFHandlerException, IOException
    {
        File file = new File( name + ".rdfn3" );
        FileWriter fileWriter = new FileWriter( file );
        RDFWriterFactory writerFactory = new N3WriterFactory();
        RDFWriter writer = writerFactory.getWriter( fileWriter );
        writeOutput( writer, graph );
        fileWriter.close();
        System.out.println( "RDF/N3 written to " + file.getAbsolutePath() );
    }

    private void writeXml( Graph graph, String name )
        throws RDFHandlerException, IOException
    {
        File file = new File( name + ".rdfxml" );
        FileWriter fileWriter = new FileWriter( file );
        RDFWriterFactory writerFactory = new RDFXMLWriterFactory();
        RDFWriter writer = writerFactory.getWriter( fileWriter );
        writeOutput( writer, graph );
        fileWriter.close();
        System.out.println( "RDF/XML written to " + file.getAbsolutePath() );
    }

    private void writeOutput( RDFWriter writer, Graph graph )
        throws RDFHandlerException
    {
        writer.startRDF();
        writer.handleNamespace( "qi4j", Qi4jRdf.QI4JMODEL );
        writer.handleNamespace( "rdf", Rdfs.RDF );
        writer.handleNamespace( "rdfs", Rdfs.RDFS );
        for( Statement st : graph )
        {
            writer.handleStatement( st );
        }
        writer.endRDF();
    }

    @Mixins( AMixin.class )
    public interface A
    {
        String doStuff();
    }

    public static class AMixin
        implements A
    {
        @This B bRef;

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

    public static class OtherStuffConcern extends ConcernOf<B>
        implements B
    {
        public String otherStuff()
        {
            return next.otherStuff() + "!";
        }
    }

    public static class LogSideEffect extends SideEffectOf<InvocationHandler>
        implements InvocationHandler
    {
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
