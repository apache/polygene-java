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
package org.apache.polygene.sample.forum.web;

import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.library.rest.server.api.ContextRestlet;
import org.apache.polygene.sample.forum.assembler.ForumAssembler;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;
import org.restlet.service.MetadataService;

/**
 * TODO
 */
public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        Energy4Java polygene = new Energy4Java(  );

        Server server = new Server( Protocol.HTTP, 8888 );

        Application app = polygene.newApplication( new ForumAssembler(), new MetadataService() );

        app.activate();

        ContextRestlet restlet = app.findModule( "REST", "Restlet" ).newObject( ContextRestlet.class, new org.restlet.Context() );

        ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put("rickard", "secret".toCharArray());
        guard.setVerifier(mapVerifier);

        guard.setNext(restlet);

        server.setNext( restlet );
        server.start();
    }
}
