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
package org.qi4j.library.framework.rdf.parse.model;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.qi4j.library.framework.rdf.Qi4jRdf;
import org.qi4j.library.framework.rdf.parse.ParseContext;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.injection.InjectionModel;

public class FieldParser
{
    private final ParseContext context;

    public FieldParser( ParseContext context )
    {
        this.context = context;
    }

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
}
