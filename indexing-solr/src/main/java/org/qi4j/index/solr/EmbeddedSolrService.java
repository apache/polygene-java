package org.qi4j.index.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.spi.service.ServiceDescriptor;

import java.io.File;
import java.lang.reflect.Field;

@Mixins(EmbeddedSolrService.EmbeddedSolrServiceMixin.class)
public interface EmbeddedSolrService extends Activatable, ServiceComposite
{
   public SolrServer getSolrServer();

   public SolrCore getSolrCore();

   abstract class EmbeddedSolrServiceMixin
         implements Activatable,EmbeddedSolrService
   {
      @Service
      FileConfiguration fileConfig;
      public CoreContainer coreContainer;
      public EmbeddedSolrServer server;

      @Uses
      ServiceDescriptor descriptor;

      private SolrCore core;

      public void activate() throws Exception
      {
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         try
         {
            File directory = new File( fileConfig.dataDirectory(), descriptor.identity() );
            directory.mkdir();

            System.setProperty( "solr.solr.home", directory.getAbsolutePath() );

            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer( coreContainer, "" );
            core = coreContainer.getCore( "" );
         } finally
         {
            Thread.currentThread().setContextClassLoader( oldCl );
         }
      }

      public void passivate() throws Exception
      {
         core.closeSearcher();
         coreContainer.shutdown();

         // Clear instance fields for GC purposes
         Field instanceField = SolrCore.class.getDeclaredField( "instance" );
         instanceField.setAccessible( true );
         instanceField.set( null, null );

         SolrConfig.config = null;
      }

      public SolrServer getSolrServer()
      {
         return server;
      }

      public SolrCore getSolrCore()
      {
         return core;
      }
   }
}
