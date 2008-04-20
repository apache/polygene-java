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

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.parse.ParseContext;
import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.LayerModel;

public final class ApplicationParser
{
    private final ParseContext context;

    public ApplicationParser( ParseContext context )
    {
        this.context = context;
    }

    public void parseModel( ApplicationModel model )
    {
        String name = model.getName();
        URI appUri = context.getApplicationURI();
        context.addName( appUri, name );
        context.addType( appUri, Qi4jRdf.TYPE_APPLICATION );
        LayerParser layerParser = context.getParserFactory().newLayerParser();
        for( LayerModel layerModel : model.getLayerModels() )
        {
            Value layer = layerParser.parseModel( layerModel );
            context.addRelationship( appUri, Qi4jRdf.RELATIONSHIP_LAYER, layer );
        }
    }
}
