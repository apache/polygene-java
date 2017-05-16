/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.library.rest.client.responsereader;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.library.rest.client.spi.ResponseReader;
import org.apache.polygene.spi.serialization.JsonDeserializer;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.resource.ResourceException;

/**
 * ResponseReader for simple types from JSON
 */
public class DefaultResponseReader
    implements ResponseReader
{
    @Structure
    private ModuleDescriptor module;

    @Service
    private JsonDeserializer jsonDeserializer;

    @Override
    public Object readResponse( Response response, Class<?> resultType ) throws ResourceException
    {
        if( MediaType.APPLICATION_JSON.equals( response.getEntity().getMediaType() ) )
        {
            if( resultType.equals( String.class ) || Number.class.isAssignableFrom( resultType ) )
            {
                try
                {
                    return jsonDeserializer.deserialize( module, resultType, response.getEntityAsText() );
                }
                catch( Exception e )
                {
                    throw new ResourceException( e );
                }
            }
        }
        return null;
    }
}
