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
package org.qi4j.library.framework.rdf.parse;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.qi4j.library.framework.rdf.Qi4jRdf;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

public final class ModuleParser
{
    private final ParseContext context;

    public ModuleParser( ParseContext context )
    {
        this.context = context;
    }

    public URI parseModel( LayerModel layerModel, ModuleModel moduleModel )
    {
        URI module = context.createModuleUri( layerModel, moduleModel );
        parsePublicComposites( module, layerModel, moduleModel );
        parsePrivateComposites( module, layerModel, moduleModel );
        parseObjectModels( module, moduleModel );
        return module;
    }

    private void parsePublicComposites( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        CompositeParser parser = context.getParserFactory().newCompositeParser();
        for( CompositeModel compositeModel : moduleModel.getPublicComposites() )
        {
            URI composite = parser.parseModel( layerModel, moduleModel, compositeModel );
            context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PUBLIC_COMPOSITE, composite );
            context.addRelationship( module, Qi4jRdf.TYPE_COMPOSITE, composite );
        }
    }

    private void parsePrivateComposites( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        CompositeParser parser = context.getParserFactory().newCompositeParser();
        for( CompositeModel compositeModel : moduleModel.getPrivateComposites() )
        {
            URI composite = parser.parseModel( layerModel, moduleModel, compositeModel );
            context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PRIVATE_COMPOSITE, composite );
            context.addRelationship( module, Qi4jRdf.TYPE_COMPOSITE, composite );
        }
    }

    private void parseObjectModels( URI module, ModuleModel model )
    {
        ObjectParser parser = context.getParserFactory().newObjectParser();
        for( ObjectModel objectModel : model.getObjectModels() )
        {
            Value object = parser.parseModel( objectModel );
            context.addRelationship( module, Qi4jRdf.TYPE_OBJECT, object );
        }
    }
}
