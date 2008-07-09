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
package org.qi4j.library.rdf.parse;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.structure.Layer;
import org.qi4j.structure.Module;

public final class ParseContext
{
    private final Graph graph;
    private final ValueFactory valueFactory;
    private final URI applicationURI;
    private final ParserFactory parserFactory;

    protected ParseContext( Graph graph, ParserFactory factory, String applicationURI )
    {
        this.parserFactory = factory;
        this.valueFactory = graph.getValueFactory();
        this.applicationURI = valueFactory.createURI( applicationURI );
        this.graph = graph;
    }

    public Graph getGraph()
    {
        return graph;
    }

    public ValueFactory getValueFactory()
    {
        return valueFactory;
    }


    public ParserFactory getParserFactory()
    {
        return parserFactory;
    }

    public URI getApplicationURI()
    {
        return applicationURI;
    }

    public URI createServiceUri( Layer layer, Module module, Class type, String identity )
    {
        String serviceType = normalizeClassToURI( type );
        URI moduleUri = createModuleUri( layer, module );
        URI uri = valueFactory.createURI( moduleUri + "/" + serviceType + "/" + identity );
        setNameAndType( uri, identity, Qi4jRdf.TYPE_SERVICE );
        return uri;
    }

    public URI createCompositeUri( Layer layer, Module module, Class composite )
    {
        String compositeName = normalizeClassToURI( composite );
        URI uri = valueFactory.createURI( createModuleUri( layer, module ) + "/" + compositeName );
        setNameAndType( uri, compositeName, Qi4jRdf.TYPE_COMPOSITE );
        return uri;
    }

    public URI createModuleUri( Layer layer, Module module )
    {
        String moduleName = module.name();
        URI uri = valueFactory.createURI( createLayerUri( layer ).toString() + "/" + moduleName );
        setNameAndType( uri, moduleName, Qi4jRdf.TYPE_MODULE );
        return uri;
    }

    public URI createLayerUri( Layer layer )
    {
        String layerName = layer.name();
        URI uri = valueFactory.createURI( applicationURI.toString() + "/" + layerName );
        setNameAndType( uri, layerName, Qi4jRdf.TYPE_LAYER );
        return uri;
    }

    public void setNameAndType( URI node, String name, URI type )
    {
        addName( node, name );
        addType( node, type );
    }


    public void addName( Resource subject, String name )
    {
        Value nameValue = valueFactory.createLiteral( name );
        graph.add( valueFactory.createStatement( subject, Rdfs.LABEL, nameValue ) );
    }

    public void addType( Resource subject, URI type )
    {
        Statement statement = valueFactory.createStatement( subject, Rdfs.TYPE, type );
        graph.add( statement );
    }

    public void addStatement( Resource subject, URI predicate, String literal )
    {
        Literal object = valueFactory.createLiteral( literal );
        Statement statement = valueFactory.createStatement( subject, predicate, object );
        graph.add( statement );
    }

    public void addRelationship( Resource subject, URI relationship, Value object )
    {
        Statement statement = valueFactory.createStatement( subject, relationship, object );
        graph.add( statement );
    }

    public void addStatement( Resource subject, URI predicate, boolean literal )
    {
        Literal object = valueFactory.createLiteral( literal );
        Statement statement = valueFactory.createStatement( subject, predicate, object );
        graph.add( statement );
    }

    public static String normalizeClassToURI( Class clazz )
    {
        String serviceType = clazz.getName();
        return serviceType.replace( '.', '_' ).replace( '$', '-' ); // TODO: Is this a good algorithm?
    }
}
