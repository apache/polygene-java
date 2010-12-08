package org.qi4j.library.osgi;

import java.util.Dictionary;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;

@Mixins( OSGiEnabledService.OSGiEnabledServiceMixin.class )
public interface OSGiEnabledService extends Activatable, ServiceComposite
{

    public abstract class OSGiEnabledServiceMixin
        implements OSGiEnabledService
    {
        @Structure
        private Module module;

        private ServiceRegistration registration;

        public void activate()
            throws Exception
        {
            BundleContext context = metaInfo( BundleContext.class );
            if( context == null )
            {
                return;
            }
            Iterable<ServiceReference<Object>> services = module.serviceFinder().findServices( type() );
            for( ServiceReference ref : services )
            {
                if( ref.identity().equals( identity().get() ) )
                {
                    Set<Class> classesSet = Classes.classesOf( type() );
                    Dictionary properties = metaInfo( Dictionary.class );
                    String[] clazzes = fetchInterfacesImplemented( classesSet );
                    registration = context.registerService( clazzes, ref.get(), properties );
                }
            }
        }

        private String[] fetchInterfacesImplemented( Set<Class> classesSet )
        {
            String[] clazzes = new String[classesSet.size()];
            int i = 0;
            for( Class clazz : classesSet )
            {
                clazzes[ i++ ] = clazz.getName();
            }
            return clazzes;
        }

        public void passivate()
            throws Exception
        {
            if( registration != null )
            {
                registration.unregister();
                registration = null;
            }
        }
    }
}
