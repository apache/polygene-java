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
package org.qi4j.library.rdf.parse.model;

import org.qi4j.library.rdf.serializer.SerializerContext;

public class FieldParser
{
    private final SerializerContext context;

    public FieldParser( SerializerContext context )
    {
        this.context = context;
    }

/*
    public Value parseModel( FieldModel fieldModel )
    {
        BNode node = createField( fieldModel );
        InjectionModel injectionModel = fieldModel.getInjectionModel();
        InjectionParser parser = context.getParserFactory().newInjectionParser();
        Value injection = parser.parseModel( injectionModel );
        context.addRelationship( node, Qi4jRdf.RELATIONSHIP_INJECTION, injection );
        return node;
    }

    private BNode createField( FieldModel fieldModel )
    {
        BNode node = context.getValueFactory().createBNode( fieldModel.getField().getName() );
        context.addType( node, Qi4jRdf.TYPE_FIELD );
        return node;
    }
*/
}
