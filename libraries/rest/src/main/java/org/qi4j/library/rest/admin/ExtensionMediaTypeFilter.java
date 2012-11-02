/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.rest.admin;

import java.util.Collections;
import java.util.List;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.routing.Filter;
import org.restlet.service.MetadataService;

/**
 * Check the extension used and set the corresponding media type
 * in the request. Then remove the extension from the request resource name.
 */
public class ExtensionMediaTypeFilter
    extends Filter
{

    public ExtensionMediaTypeFilter()
    {
    }

    public ExtensionMediaTypeFilter( Context context )
    {
        super( context );
    }

    public ExtensionMediaTypeFilter( Context context, Restlet next )
    {
        super( context, next );
    }

    @Override
    protected int beforeHandle( Request request, Response response )
    {
        List<String> segments = request.getResourceRef().getSegments();
        if (segments.get( segments.size()-1 ).equals(""))
          return Filter.CONTINUE;

        String extensions = request.getResourceRef().getExtensions();
        if( extensions != null )
        {
            int idx = extensions.lastIndexOf( "." );
            if( idx != -1 )
            {
                extensions = extensions.substring( idx + 1 );
            }

            MetadataService metadataService = getApplication().getMetadataService();
            Metadata metadata = metadataService.getMetadata( extensions );
            if( metadata != null && metadata instanceof MediaType )
            {
                request.getClientInfo()
                    .setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( (MediaType) metadata ) ) );
                String path = request.getResourceRef().getPath();
                path = path.substring( 0, path.length() - extensions.length() - 1 );
                request.getResourceRef().setPath( path );
            }
        }

        return Filter.CONTINUE;
    }
}
