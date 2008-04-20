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

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.parse.ParseContext;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.ParameterModel;

public final class MethodParser
{
    private final ParseContext context;

    public MethodParser( ParseContext context )
    {
        this.context = context;
    }

    public Value parseModel( MethodModel methodModel )
    {
        BNode node = createMethod( methodModel );
        context.addStatement( node, Qi4jRdf.HAS_INJECTIONS, methodModel.hasInjections() );

        ParameterParser parser = context.getParserFactory().newParameterParser();
        for( ParameterModel parameterModel : methodModel.getParameterModels() )
        {
            Value parameter = parser.parseModel( parameterModel );
            context.addRelationship( node, Qi4jRdf.TYPE_PARAMETER, parameter );
        }
        return node;
    }

    private BNode createMethod( MethodModel methodModel )
    {
        BNode node = context.getValueFactory().createBNode( methodModel.getMethod().getName() );
        context.addType( node, Qi4jRdf.TYPE_METHOD );
        return node;
    }
}
