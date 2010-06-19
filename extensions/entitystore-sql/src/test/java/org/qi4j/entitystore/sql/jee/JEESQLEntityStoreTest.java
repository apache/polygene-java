package org.qi4j.entitystore.sql.jee;

import java.io.File;
import java.io.IOException;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.ScatteredArchive;
import org.glassfish.api.embedded.Server;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.junit.Test;

@SuppressWarnings( { "UseOfSystemOutOrSystemErr", "CallToThreadDumpStack" } )
public class JEESQLEntityStoreTest
{

    @Ignore
    @Test
    public void test()
            throws InterruptedException
    {
        try {

            EmbeddedFileSystem.Builder fsBuilder = new EmbeddedFileSystem.Builder();
            fsBuilder.autoDelete( true );
            fsBuilder.installRoot( new File( "target/gf-install" ) );
            fsBuilder.instanceRoot( new File( "target/gf-instance" ) );
            EmbeddedFileSystem fs = fsBuilder.build();

            Server.Builder serverBuilder = new Server.Builder( "qi4j" );
            serverBuilder.embeddedFileSystem( fs );
            Server server = serverBuilder.build();
            server.createPort( 8888 );
            server.start();

            EmbeddedDeployer deployer = server.getDeployer();
            ScatteredArchive.Builder warBuilder = new ScatteredArchive.Builder( "qi4j", new File( "target/gf-archive" ) );
            warBuilder.resources( new File( "src/test/resources" ) );
            //warBuilder.addClassPath( new File( "src/main/java" ) );
            warBuilder.addClassPath( new File( "src/test/java" ) );

            ScatteredArchive war = warBuilder.buildWar();
            System.out.println( "PROUT" );
            DeployCommandParameters deployParams = new DeployCommandParameters();
            deployParams.contextroot = "qi4j";
            deployer.deploy( war, deployParams );

            System.out.println( "ALL IS RUNNING, WAITING 5 SEC BEFORE SHUTDOWN" );
            Thread.sleep( 5000 );

            deployer.undeployAll();
            server.stop();

        } catch ( LifecycleException ex ) {
            ex.printStackTrace();
            fail( ex.getMessage() );
        } catch ( IOException ex ) {
            ex.printStackTrace();
            fail( ex.getMessage() );
        }
    }

}
