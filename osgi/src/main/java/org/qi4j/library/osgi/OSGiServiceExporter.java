package org.qi4j.library.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.HasMetaInfo;
import org.qi4j.api.util.Classes;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

@Mixins( OSGiServiceExporter.OSGiServiceExporterMixin.class )
public interface OSGiServiceExporter extends Activatable, ServiceComposite
{

    public static abstract class OSGiServiceExporterMixin
        implements OSGiServiceExporter
    {
        @Service
        @HasMetaInfo( BundleContext.class )
        private Iterable<ServiceReference<ServiceComposite>> services;
        private ArrayList<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

        public void activate()
            throws Exception
        {
            for( ServiceReference<ServiceComposite> ref : services )
            {
                Class<? extends BundleContext> type = BundleContext.class;
                BundleContext context = ref.metaInfo( type );
                ServiceComposite service = ref.get();
                Set<Class> interfaces = Classes.interfacesOf( service.getClass() );
                String[] interfaceNames = new String[interfaces.size()];
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
                    interfaceNames[ i++ ] = cls.getName();
                }
                registrations.add( context.registerService( interfaceNames, service, properties ) );

            }
        }

        public void passivate()
            throws Exception
        {
            for( ServiceRegistration reg : registrations )
            {
                reg.unregister();
            }
        }
    }
}
