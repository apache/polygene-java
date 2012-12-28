/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.client.api;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.client.ClientCache;
import org.qi4j.library.rest.client.RequestWriterDelegator;
import org.qi4j.library.rest.client.ResponseReaderDelegator;
import org.qi4j.library.rest.client.spi.RequestWriter;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.qi4j.library.rest.client.spi.ResponseReader;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Reference;

/**
 * Builder for UsecaseClient
 */
public class ContextResourceClientFactory
{
    @Structure
    private Module module;

    @Uses
    @Optional
    private ClientCache cache;

    @Uses
    private ResponseReaderDelegator readerDelegator;

    @Uses
    private RequestWriterDelegator requestWriterDelegator;

    @Uses
    private Uniform client;

    private ClientInfo info = new ClientInfo();
    private Form requestHeaders = new Form();

    private ResponseHandler errorHandler;

    public ContextResourceClient newClient( Reference reference )
    {
        ContextResourceClient contextResourceClient = module.newObject( ContextResourceClient.class, this, reference );
        contextResourceClient.onError( errorHandler );
        return contextResourceClient;
    }

    public void setAgent(String agent)
    {
        info.setAgent( agent );
    }

    public Form getCustomRequestHeaders()
    {
        return requestHeaders;
    }

    public void setAcceptedLanguages(Language... acceptedLanguages)
    {
        List<Preference<Language>> languages = new ArrayList<Preference<Language>>();
        for( Language acceptedLanguage : acceptedLanguages )
        {
            languages.add( new Preference<Language>( ));
        }

        info.setAcceptedLanguages( languages );

    }

    public void setAcceptedMediaTypes(MediaType... acceptedMediaTypes)
    {
        List<Preference<MediaType>> mediatypes = new ArrayList<Preference<MediaType>>();
        for( MediaType mediaType : acceptedMediaTypes )
        {
            mediatypes.add( new Preference<MediaType>(mediaType) );
        }

        info.setAcceptedMediaTypes( mediatypes );
    }

    public ClientInfo getInfo()
    {
        return info;
    }

    public void setErrorHandler( ResponseHandler errorHandler )
    {
        this.errorHandler = errorHandler;
    }

    public ResponseHandler getErrorHandler()
    {
        return errorHandler;
    }

    // Internal
    void updateCommandRequest( Request request )
    {
        request.setClientInfo( info );

        // Update cache information
        if( cache != null )
        {
            cache.updateCommandConditions( request );
        }

        // Add all custom headers
        if (!requestHeaders.isEmpty())
            request.getAttributes().put( "org.restlet.http.headers", requestHeaders );
    }

    void updateQueryRequest( Request request )
    {
        request.setClientInfo( info );

        // Update cache information
        if( cache != null )
        {
            cache.updateQueryConditions( request );
        }

    }

    void updateCache( Response response )
    {
        if( cache != null )
        {
            cache.updateCache( response );
        }
    }

    Uniform getClient()
    {
        return client;
    }

    public <T> T readResponse( Response response, Class<T> queryResult )
    {
        return (T) readerDelegator.readResponse( response, queryResult );
    }

    public void writeRequest( Request request, Object queryRequest )
    {
        if( !requestWriterDelegator.writeRequest( queryRequest, request ) )
        {
            throw new IllegalArgumentException( "Illegal query request type:" + queryRequest.getClass().getName() );
        }
    }

    public void registerResponseReader(ResponseReader reader)
    {
        readerDelegator.registerResponseReader( reader );
    }

    public void registerRequestWriter(RequestWriter writer)
    {
        requestWriterDelegator.registerRequestWriter( writer );
    }
}
