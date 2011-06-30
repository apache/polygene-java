package org.qi4j.library.sql.liquibase;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper service for Liquibase
 */
@Mixins(LiquibaseService.Mixin.class)
public interface LiquibaseService
      extends Activatable, ServiceComposite
{
   class Mixin
         implements Activatable
   {
      @This
      Configuration<LiquibaseConfiguration> config;

      @Service
      ServiceReference<DataSource> dataSource;

      public void activate() throws Exception
      {
         Logger log = LoggerFactory.getLogger(LiquibaseService.class.getName());

         boolean enabled = config.configuration().enabled().get();
         if (!enabled || !dataSource.isAvailable())
         {
            return;
         }

         Connection c = null;
         try
         {
            c = dataSource.get().getConnection();
            DatabaseConnection dc = new JdbcConnection(c);
            Liquibase liquibase = new Liquibase( config.configuration().changeLog().get(),
                  new ClassLoaderResourceAccessor(),dc );

            liquibase.update( config.configuration().contexts().get() );
         }
         catch (SQLException e)
         {
            Throwable ex = e;
            while (ex.getCause() != null)
               ex = ex.getCause();

            if (ex instanceof ConnectException)
            {
               log.warn( "Could not connect to database; Liquibase should be disabled" );
               return;
            }

            log.error( "Liquibase could not perform database migration", e );

            if (c != null)
               try
               {
                  c.rollback();
                  c.close();
               }
               catch (SQLException ex1)
               {
               }
         } catch (ServiceImporterException ex)
         {
            log.warn( "DataSource is not available - database refactoring skipped" );
         }
      }

      public void passivate() throws Exception
      {
      }

   }
}