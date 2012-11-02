/*
 * Copyright 2007, 2008 Niclas Hedhman.
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
package org.qi4j.library.rdf.serializer;

import java.lang.reflect.Method;
import org.openrdf.model.*;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Rdfs;

public final class SerializerContext
{
    private final Graph graph;
    private final ValueFactory valueFactory;

    public SerializerContext( Graph graph )
    {
        this.valueFactory = graph.getValueFactory();
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

    public String createServiceUri( String layer, String module, Class type, String identity )
    {
        String serviceType = Classes.normalizeClassToURI( type.getName() );
        String moduleUri = createModuleUri( layer, module );
        return moduleUri + "/" + serviceType + "/" + identity;
    }

    public String createCompositeUri( String module, Class composite )
    {
        String compositeName = Classes.normalizeClassToURI( composite.getName() );
        return module + "/" + compositeName;
    }

    public String createApplicationUri( String app )
    {
        return "urn:qi4j:model:" + app;
    }

    public String createLayerUri( String appUri, String layer )
    {
        return appUri + "/" + layer;
    }

    public String createModuleUri( String layerUri, String module )
    {
        return layerUri + "/" + module;
    }

    public void setNameAndType( String node, String name, URI type )
    {
        addType( node, type );
        addName( node, name );
    }


    public void addName( String subject, String name )
    {
        Value nameValue = valueFactory.createLiteral( name );
        URI subjectUri = valueFactory.createURI( subject );
        graph.add( valueFactory.createStatement( subjectUri, Rdfs.LABEL, nameValue ) );
    }

    public void addType( String subject, URI type )
    {
        URI subjectUri = valueFactory.createURI( subject );
        Statement statement = valueFactory.createStatement( subjectUri, Rdfs.TYPE, type );
        graph.add( statement );
    }

    public void addStatement( String subject, URI predicate, String literal )
    {
        Literal object = valueFactory.createLiteral( literal );
        URI subjectUri = valueFactory.createURI( subject );
        Statement statement = valueFactory.createStatement( subjectUri, predicate, object );
        graph.add( statement );
    }

    public void addRelationship( String subject, URI relationship, String object )
    {
        URI subjectUri = valueFactory.createURI( subject );
        URI objectUri = valueFactory.createURI( object );
        Statement statement = valueFactory.createStatement( subjectUri, relationship, objectUri );
        graph.add( statement );
    }

    public void addStatement( String subject, URI predicate, boolean literal )
    {
        URI subjectUri = valueFactory.createURI( subject );
        Literal object = valueFactory.createLiteral( literal );
        Statement statement = valueFactory.createStatement( subjectUri, predicate, object );
        graph.add( statement );
    }

    public String createCompositeMethodUri( String compositeUri, Method method )
    {
        return compositeUri + "/" + method.toGenericString().replace( " ", "_" );
    }
}
