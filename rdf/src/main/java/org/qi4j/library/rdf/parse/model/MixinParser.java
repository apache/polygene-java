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

public final class MixinParser
{
    private final SerializerContext context;

    public MixinParser( SerializerContext context )
    {
        this.context = context;
    }

/*
    public Value parseModel( MixinModel model )
    {
        BNode node = createMixin( model );
        return node;
    }

    private BNode createMixin( MixinModel model )
    {
        BNode node = context.getValueFactory().createBNode( model.getClass().getName() );
        context.addType( node, Qi4jRdf.TYPE_MIXIN );
        return node;
    }

*/
}
