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
package org.qi4j.library.rdf.parse.model;

import org.qi4j.library.rdf.serializer.SerializerContext;

public final class ConcernParser
{
    private final SerializerContext context;

    public ConcernParser( SerializerContext context )
    {
        this.context = context;
    }

/*
    public Value parseModel( ConcernModel concernModel )
    {
        BNode concernNode = createConcern( concernModel );
        parseConstructors( concernModel, concernNode );
        parseFields( concernModel, concernNode );
        parseAppliesTo( concernModel, concernNode );
        parseMethods( concernModel, concernNode );
        return concernNode;
    }

    private void parseMethods( ConcernModel concernModel, BNode concernNode )
    {
        MethodParser parser = context.getParserFactory().newMethodParser();
        for( MethodModel methodModel : concernModel.getMethodModels() )
        {
            Value method = parser.parseModel( methodModel );
            context.addRelationship( concernNode, Qi4jRdf.RELATION_METHOD, method );
        }
    }

    private void parseAppliesTo( ConcernModel concernModel, BNode concernNode )
    {
        for( Class clazz : concernModel.getAppliesTo() )
        {
            Value clazzName = context.getValueFactory().createLiteral( clazz.getName() );
            context.addRelationship( concernNode, Qi4jRdf.RELATIONSHIP_APPLIESTO, clazzName );
        }
    }

    private void parseConstructors( ConcernModel concernModel, BNode concernNode )
    {
        ConstructorParser parser = context.getParserFactory().newConstructorParser();
        for( ConstructorModel constructorModel : concernModel.getConstructorModels() )
        {
            Value constructor = parser.parseModel( constructorModel );
            context.addRelationship( concernNode, Qi4jRdf.RELATIONSHIP_CONSTRUCTOR, constructor );
        }
    }

    private void parseFields( ConcernModel concernModel, BNode concernNode )
    {
        FieldParser parser = context.getParserFactory().newFieldParser();
        for( FieldModel fieldModel : concernModel.getFieldModels() )
        {
            Value field = parser.parseModel( fieldModel );
            context.addRelationship( concernNode, Qi4jRdf.RELATIONSHIP_FIELD, field );
        }
    }

    private BNode createConcern( ConcernModel concernModel )
    {
        BNode node = context.getValueFactory().createBNode( concernModel.getClass().getName() );
        context.addType( node, Qi4jRdf.TYPE_CONCERN );
        return node;
    }
*/
}
