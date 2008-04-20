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
import org.openrdf.model.ValueFactory;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.parse.ParseContext;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceInstanceProvider;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

public class ServiceParser
{
    private final ParseContext context;

    public ServiceParser( ParseContext context )
    {
        this.context = context;
    }

    public Value parseModel( LayerModel layerModel, ModuleModel moduleModel, ServiceDescriptor descriptor )
    {
        ValueFactory valueFactory = context.getValueFactory();
        String identity = descriptor.identity();
        Class type = descriptor.gerviceType();
        URI serviceNode = context.createServiceUri( layerModel, moduleModel, type, identity );
        Class<? extends ServiceInstanceProvider> serviceProvider = descriptor.serviceProvider();
        String providerName = ParseContext.normalizeClassToURI( serviceProvider );
        context.addStatement( serviceNode, Qi4jRdf.RELATIONSHIP_PROVIDEDBY, providerName );
        Iterable<Class> infos = descriptor.serviceAttributeTypes();
        for( Class info : infos )
        {
            String infoName = ParseContext.normalizeClassToURI( info );
            URI infoNode = valueFactory.createURI( serviceNode.toString() + "/" + infoName );
            context.addType( infoNode, Qi4jRdf.TYPE_INFO );
            context.addRelationship( serviceNode, Qi4jRdf.RELATIONSHIP_SERVICEINFO, infoNode );
            Value value = valueFactory.createLiteral( descriptor.serviceAttribute( info ).toString() );
            context.addRelationship( infoNode, Qi4jRdf.RELATIONSHIP_INFOVALUE, value );
        }
        return serviceNode;
    }
}
