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

package org.apache.polygene.library.rdf;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.sideeffect.SideEffectOf;
import org.apache.polygene.api.sideeffect.SideEffects;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.rdf.model.ApplicationSerializer;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.n3.N3WriterFactory;
import org.openrdf.rio.rdfxml.RDFXMLWriterFactory;

/**
 * JAVADOC
 */
public class ApplicationXmlTest extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        LayerAssembly layerAssembly = module.layer();
        layerAssembly.application().setName( "testapp" );
        module.transients( TestComposite.class );
    }

    @Test
    public void testApplicationXml()
        throws Exception
    {
        ApplicationSerializer parser = new ApplicationSerializer();
        Iterable<Statement> graph = parser.serialize( application ); // TODO Fix this
        writeN3( graph );
        writeXml( graph );
    }

    private void writeN3( Iterable<Statement> graph )
        throws RDFHandlerException, IOException
    {
        RDFWriterFactory writerFactory = new N3WriterFactory();
        RDFWriter writer = writerFactory.getWriter( System.out );
        writeOutput( writer, graph );
    }

    private void writeXml( Iterable<Statement> graph )
        throws RDFHandlerException, IOException
    {
        RDFWriterFactory writerFactory = new RDFXMLWriterFactory();
        RDFWriter writer = writerFactory.getWriter( System.out );
        writeOutput( writer, graph );
    }

    private void writeOutput( RDFWriter writer, Iterable<Statement> graph )
        throws RDFHandlerException
    {
        writer.startRDF();
        writer.handleNamespace( "polygene", PolygeneRdf.POLYGENE_MODEL );
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
        extends A, TransientComposite
    {
    }
}
