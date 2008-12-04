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

public final class ModuleParser
{
    private final SerializerContext context;

    public ModuleParser( SerializerContext context )
    {
        this.context = context;
    }

/*
    public URI parseModel( LayerModel layerModel, ModuleModel moduleModel )
    {
        URI module = context.createModuleUri( layerModel, moduleModel );
        parseComposites( module, layerModel, moduleModel );
        parseObjects( module, moduleModel );
        parseServices( module, layerModel, moduleModel );
        parseProperties( module, layerModel, moduleModel );
        parseAssociations( module, layerModel, moduleModel );
        return module;
    }

    private void parseComposites( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        CompositeParser parser = context.getParserFactory().newCompositeParser();
        for( CompositeDescriptor compositeDescriptor : moduleModel.compositeDescriptors() )
        {
            URI composite = parser.parseModel( layerModel, moduleModel, compositeDescriptor.getCompositeModel() );
            if( compositeDescriptor.getVisibility() == Visibility.module )
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_COMPOSITE, composite );
            }
            else
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PUBLIC_COMPOSITE, composite );
            }
        }
    }

    private void parseObjects( URI module, ModuleModel model )
    {
        ObjectParser parser = context.getParserFactory().newObjectParser();
        for( ObjectDescriptor objectDescriptor : model.objectDescriptors() )
        {
            Value object = parser.parseModel( objectDescriptor.getObjectModel() );
            if( objectDescriptor.getVisibility() != Visibility.module )
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PUBLIC_OBJECT, object );
            }
            else
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_OBJECT, object );
            }
        }
    }

    private void parseServices( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        ServiceParser parser = context.getParserFactory().newServiceParser();
        Iterable<ServiceDescriptor> descriptors = moduleModel.serviceDescriptors();
        for( ServiceDescriptor descriptor : descriptors )
        {
            Value service = parser.parseModel( layerModel, moduleModel, descriptor );
            if( descriptor.visibility() != Visibility.module )
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PUBLIC_SERVICE, service );
            }
            else
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PRIVATE_SERVICE, service );
            }
        }
    }

    private void parseAssociations( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        //TODO: Auto-generated, need attention.

    }

    private void parseProperties( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        //TODO: Auto-generated, need attention.

    }

*/
}
