package org.qi4j.library.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Iterables;

import java.lang.reflect.Type;
import java.util.Dictionary;
import java.util.Set;

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
            Iterable<ServiceReference<Object>> services = module.serviceFinder().findServices( (Class)type() );
            for( ServiceReference ref : services )
            {
                if( ref.identity().equals( identity().get() ) )
                {
                    Iterable<Type> classesSet = Classes.TYPES_OF.map( type() );
                    Dictionary properties = metaInfo( Dictionary.class );
                    String[] clazzes = fetchInterfacesImplemented( classesSet );
                    registration = context.registerService( clazzes, ref.get(), properties );
                }
            }
        }

        private String[] fetchInterfacesImplemented( Iterable<Type> classesSet )
        {
            String[] clazzes = new String[(int) Iterables.count( classesSet)];
            int i = 0;
            for( Type clazz : classesSet )
            {
                clazzes[ i++ ] = Classes.RAW_CLASS.map( clazz ).getName();
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
