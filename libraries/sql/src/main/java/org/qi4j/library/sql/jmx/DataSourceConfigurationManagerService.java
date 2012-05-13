package org.qi4j.library.sql.jmx;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.datasource.C3P0DataSourceServiceImporter;

import javax.management.*;
import javax.sql.DataSource;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.spi.Qi4jSPI;

/**
 * Expose DataSourceConfiguration through JMX. Allow configurations to be edited, and the services to be restarted.
 */
@Mixins(DataSourceConfigurationManagerService.Mixin.class)
public interface DataSourceConfigurationManagerService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      @Structure
      Module module;

      @Service
      MBeanServer server;

      @Structure
      Qi4jSPI spi;

      @Structure
      Application application;

      @Service
      Iterable<ServiceReference<DataSource>> dataSources;
      @Service
      ServiceReference<C3P0DataSourceServiceImporter> dataSourceService;

      private List<ObjectName> configurationNames = new ArrayList<ObjectName>();

      public void activate() throws Exception
      {
         // Expose configurable services
         exportDataSources();
      }

      private void exportDataSources() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException
      {
         for (ServiceReference<DataSource> dataSource : dataSources)
         {
            String name = dataSource.identity();
            Module module = (Module) spi.getModule( dataSource );
            EntityDescriptor descriptor = module.entityDescriptor( DataSourceConfiguration.class.getName() );
            List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
            Map<String, AccessibleObject> properties = new LinkedHashMap<String, AccessibleObject>();
            for (PropertyDescriptor persistentProperty : descriptor.state().properties())
            {
               if ( !persistentProperty.isImmutable())
               {
                  String propertyName = persistentProperty.qualifiedName().name();
                  String type = persistentProperty.valueType().mainType().getName();
                  attributes.add( new MBeanAttributeInfo( propertyName, type, propertyName, true, true, type.equals( "java.lang.Boolean" ) ) );
                  properties.put( propertyName, persistentProperty.accessor() );
               }
            }

            List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
            operations.add( new MBeanOperationInfo( "restart", "Restart DataSource", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION_INFO ) );

            MBeanInfo mbeanInfo = new MBeanInfo( DataSourceConfiguration.class.getName(), name, attributes.toArray( new MBeanAttributeInfo[attributes.size()] ), null, operations.toArray( new MBeanOperationInfo[operations.size()] ), null );
            Object mbean = new ConfigurableDataSource( dataSourceService, mbeanInfo, name, properties );
            ObjectName configurableDataSourceName = new ObjectName( "Qi4j:application="+application.name()+",class=Datasource,name=" + name );
            server.registerMBean( mbean, configurableDataSourceName );
            configurationNames.add( configurableDataSourceName );
         }
      }

      public void passivate() throws Exception
      {
         for (ObjectName configurableServiceName : configurationNames)
         {
            server.unregisterMBean( configurableServiceName );
         }
      }

      abstract class EditableConfiguration
            implements DynamicMBean
      {
         MBeanInfo info;
         String identity;
         Map<String, AccessibleObject> propertyNames;

         EditableConfiguration( MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
         {
            this.info = info;
            this.identity = identity;
            this.propertyNames = propertyNames;
         }

         public Object getAttribute( String name ) throws AttributeNotFoundException, MBeanException, ReflectionException
         {
            UnitOfWork uow = module.newUnitOfWork();
            try
            {
               EntityComposite configuration = uow.get( EntityComposite.class, identity );
               AssociationStateHolder state = spi.getState( configuration );
               AccessibleObject accessor = propertyNames.get( name );
               Property<Object> property = state.propertyFor( accessor );
               return property.get();
            } catch (Exception ex)
            {
               throw new ReflectionException( ex, "Could not get attribute " + name );
            } finally
            {
               uow.discard();
            }
         }

         public void setAttribute( Attribute attribute ) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
         {
            UnitOfWork uow = module.newUnitOfWork();
            try
            {
               EntityComposite configuration = uow.get( EntityComposite.class, identity );
               AssociationStateHolder state = spi.getState( configuration );
               AccessibleObject accessor = propertyNames.get( attribute.getName() );
               Property<Object> property = state.propertyFor( accessor );
               property.set( attribute.getValue() );
                try
                {
                    uow.complete();
                }
                catch( UnitOfWorkCompletionException e )
                {
                    throw new ReflectionException( e );
                }
            } finally
            {
               uow.discard();
            }
         }

         public AttributeList getAttributes( String[] names )
         {
            AttributeList list = new AttributeList();
            for (String name : names)
            {
               try
               {
                  Object value = getAttribute( name );
                  list.add( new Attribute( name, value ) );
               } catch (AttributeNotFoundException e)
               {
                  e.printStackTrace();
               } catch (MBeanException e)
               {
                  e.printStackTrace();
               } catch (ReflectionException e)
               {
                  e.printStackTrace();
               }
            }

            return list;
         }

         public AttributeList setAttributes( AttributeList attributeList )
         {
            AttributeList list = new AttributeList();
            for (int i = 0; i < list.size(); i++)
            {
               Attribute attribute = (Attribute) list.get( i );

               try
               {
                  setAttribute( attribute );
                  list.add( attribute );
               } catch (AttributeNotFoundException e)
               {
                  e.printStackTrace();
               } catch (InvalidAttributeValueException e)
               {
                  e.printStackTrace();
               } catch (MBeanException e)
               {
                  e.printStackTrace();
               } catch (ReflectionException e)
               {
                  e.printStackTrace();
               }
            }

            return list;
         }

         public MBeanInfo getMBeanInfo()
         {
            return info;
         }
      }

      class ConfigurableDataSource
         extends EditableConfiguration
      {
         private ServiceReference<C3P0DataSourceServiceImporter> service;

         ConfigurableDataSource( ServiceReference<C3P0DataSourceServiceImporter> service, MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
         {
            super( info, identity, propertyNames );
            this.service = service;
         }

         public Object invoke( String s, Object[] objects, String[] strings ) throws MBeanException, ReflectionException
         {
            if (s.equals( "restart" ))
            {
               try
               {
                  // Refresh and restart
                  if (service.isActive())
                  {
                     ((Activatable) service).passivate();
                     ((Activatable) service).activate();
                  }

                  return "Restarted DataSource";
               } catch (Exception e)
               {
                  return "Could not restart DataSource:" + e.getMessage();
               }
            }

            return "Unknown operation";
         }

      }
   }
}
