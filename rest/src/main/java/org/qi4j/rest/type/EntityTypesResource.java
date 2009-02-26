/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.rest.type;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.restlet.Context;
import static org.restlet.data.CharacterSet.UTF_8;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.WriterRepresentation;

public final class EntityTypesResource extends Resource
{
    private final Set<String> entityTypes;

    public EntityTypesResource( @Uses Context context,
                                @Uses Request request,
                                @Uses Response response,
                                @Structure Application application,
                                final @Structure Qi4jSPI spi,
                                final @Structure Module module )
    {
        super( context, request, response );

        List<Variant> variants = getVariants();
        variants.add( new Variant( MediaType.TEXT_HTML ) );

        entityTypes = new HashSet<String>();
        // Get all entity types
        ApplicationSPI applicationSPI = (ApplicationSPI) application;
        applicationSPI.visitDescriptor( new DescriptorVisitor()
        {
            @Override public void visit( EntityDescriptor entityDescriptor )
            {
                Class<?> entityType = entityDescriptor.type();
                if( spi.getEntityDescriptor( entityType, module ) != null )
                {
                    entityTypes.add( entityType.getName() );
                }
            }
        } );
    }

    @Override
    public Representation represent( Variant variant )
        throws ResourceException
    {
        if( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            return representHTML( entityTypes );
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private Representation representHTML( final Set<String> entityTypes )
    {
        Representation representation = new WriterRepresentation( MediaType.TEXT_HTML )
        {
            public void write( Writer buf )
                throws IOException
            {
                PrintWriter out = new PrintWriter( buf );

                String path = getRequest().getResourceRef().getPath();
                if( !path.endsWith( "/" ) )
                {
                    path += "/";
                }

                try
                {
                    out.println( "<html><head><title>All entities</title</head><body><h1>All entities</h1><ul>" );

                    for( String entityType : entityTypes )
                    {
                        out.println( "<li><a href=\"" + path + entityType + ".html\">" + entityType + "</a></li>" );
                    }

                    out.println( "</ul></body></html>" );
                }
                finally
                {
                    out.close();
                }
            }
        };
        representation.setCharacterSet( UTF_8 );
        return representation;
    }
}
