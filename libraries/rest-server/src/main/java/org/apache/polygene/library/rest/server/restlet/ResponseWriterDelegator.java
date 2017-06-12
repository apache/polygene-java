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

package org.apache.polygene.library.rest.server.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.library.rest.server.spi.ResponseWriter;
import org.restlet.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates to a list of potential writers. Register writers on startup.
 */
public class ResponseWriterDelegator
    implements ResponseWriter
{
    List<ResponseWriter> responseWriters = new ArrayList<>();

    @Structure
    Module module;

    public void init( @Service Iterable<ServiceReference<ResponseWriter>> resultWriters )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        Identity responsewriterdelegator = StringIdentity.fromString( "responsewriterdelegator" );

        // Add custom writers first
        for( ServiceReference<ResponseWriter> resultWriter : resultWriters )
        {
            if( !resultWriter.identity().equals(responsewriterdelegator) )
            {
                logger.info( "Registered result writer:" + resultWriter.identity() );
                registerResultWriter( resultWriter.get() );
            }
        }

        // Add defaults
        ResourceBundle defaultResultWriters = ResourceBundle.getBundle( "org.apache.polygene.library.rest.server.rest-server" );

        String resultWriterClasses = defaultResultWriters.getString( "responsewriters" );
        logger.info( "Using response writers:" + resultWriterClasses );
        for( String className : resultWriterClasses.split( "," ) )
        {
            try
            {
                Class writerClass = module.descriptor().classLoader().loadClass( className.trim() );
                ResponseWriter writer = (ResponseWriter) module.newObject( writerClass );
                registerResultWriter( writer );
            }
            catch( ClassNotFoundException e )
            {
                logger.warn( "Could not register response writer " + className, e );
            }
        }
    }

    public void registerResultWriter( ResponseWriter writer )
    {
        responseWriters.add( writer );
    }

    @Override
    public boolean writeResponse( Object result, Response response )
    {
        for( ResponseWriter responseWriter : responseWriters )
        {
            if( responseWriter.writeResponse( result, response ) )
            {
                return true;
            }
        }
        return false;
    }
}
