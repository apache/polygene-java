package org.qi4j.library.osgi;

import java.util.ArrayList;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.HasMetaInfo;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Iterables;

import static org.qi4j.api.util.Classes.interfacesOf;

/**
 * Export Qi4j services to an OSGi Bundle.
 */
@Mixins( OSGiServiceExporter.OSGiServiceExporterMixin.class )
@Activators( OSGiServiceExporter.Activator.class )
public interface OSGiServiceExporter
    extends ServiceComposite
{

    void registerServices()
        throws Exception;

    void unregisterServices()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<OSGiServiceExporter>>
    {

        @Override
        public void afterActivation( ServiceReference<OSGiServiceExporter> activated )
            throws Exception
        {
            activated.get().registerServices();
        }

        @Override
        public void beforePassivation( ServiceReference<OSGiServiceExporter> passivating )
            throws Exception
        {
            passivating.get().unregisterServices();
        }

    }

    public static abstract class OSGiServiceExporterMixin
        implements OSGiServiceExporter
    {

        @Service
        @HasMetaInfo( BundleContext.class )
        private Iterable<ServiceReference<ServiceComposite>> services;
        private ArrayList<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

        @Override
        public void registerServices()
            throws Exception
        {
            for( ServiceReference<ServiceComposite> ref : services )
            {
                Class<? extends BundleContext> type = BundleContext.class;
                BundleContext context = ref.metaInfo( type );
                ServiceComposite service = ref.get();
                Iterable<Class<?>> interfaces = Iterables.map( Classes.RAW_CLASS, interfacesOf( service.getClass() ) );
                String[] interfaceNames = new String[ (int) Iterables.count( interfaces ) ];
                Properties properties = ref.metaInfo( Properties.class );
                if( properties == null )
                {
                    properties = new Properties();
                }
                properties.put( "org.qi4j.api.service.active", ref.isActive() );
                properties.put( "org.qi4j.api.service.available", ref.isAvailable() );
                properties.put( "org.qi4j.api.service.identity", ref.identity() );
                int i = 0;
                for( Class cls : interfaces )
                {
                    interfaceNames[ i++] = cls.getName();
                }
                registrations.add( context.registerService( interfaceNames, service, properties ) );

            }
        }

        @Override
        public void unregisterServices()
            throws Exception
        {
            for( ServiceRegistration reg : registrations )
            {
                reg.unregister();
            }
        }

    }

}
