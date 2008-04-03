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

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.qi4j.library.framework.rdf.Qi4jRdf;
import org.qi4j.library.framework.rdf.parse.ParseContext;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;
import org.qi4j.spi.structure.ObjectDescriptor;
import org.qi4j.structure.Visibility;

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
        for( CompositeDescriptor compositeDescriptor : moduleModel.getCompositeDescriptors() )
        {
            URI composite = parser.parseModel( layerModel, moduleModel, compositeDescriptor.getCompositeModel() );
            if( compositeDescriptor.getVisibility() == Visibility.module )
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PRIVATE_COMPOSITE, composite );
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
        for( ObjectDescriptor objectDescriptor : model.getObjectDescriptors() )
        {
            Value object = parser.parseModel( objectDescriptor.getObjectModel() );
            if( objectDescriptor.getVisibility() != Visibility.module )
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PUBLIC_OBJECT, object );
            }
            else
            {
                context.addRelationship( module, Qi4jRdf.RELATIONSHIP_PRIVATE_OBJECT, object );
            }
        }
    }

    private void parseServices( URI module, LayerModel layerModel, ModuleModel moduleModel )
    {
        ServiceParser parser = context.getParserFactory().newServiceParser();
        Iterable<ServiceDescriptor> descriptors = moduleModel.getServiceDescriptors();
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

}
