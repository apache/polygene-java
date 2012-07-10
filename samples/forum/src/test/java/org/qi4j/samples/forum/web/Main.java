package org.qi4j.samples.forum.web;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.library.rest.server.api.ContextRestlet;
import org.qi4j.samples.forum.assembler.ForumAssembler;
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
        Energy4Java is = new Energy4Java(  );

        Server server = new Server( Protocol.HTTP, 8888 );

        Application app = is.newApplication( new ForumAssembler(), new MetadataService() );

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
