package org.qi4j.library.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Iterables;

import java.lang.reflect.Type;
import java.util.Dictionary;

@Mixins( OSGiEnabledService.OSGiEnabledServiceMixin.class )
public interface OSGiEnabledService extends Activatable, ServiceComposite
{

    public abstract class OSGiEnabledServiceMixin
        implements OSGiEnabledService
    {
        @Uses
        ServiceDescriptor descriptor;

        @Structure
        private Module module;

        private ServiceRegistration registration;

        public void activate()
            throws Exception
        {
            BundleContext context = descriptor.metaInfo( BundleContext.class );
            if( context == null )
            {
                return;
            }
            for( ServiceReference ref : module.findServices( descriptor.type() ) )
            {
                if( ref.identity().equals( identity().get() ) )
                {
                    Iterable<Type> classesSet = Classes.TYPES_OF.map( descriptor.type() );
                    Dictionary properties = descriptor.metaInfo( Dictionary.class );
                    String[] clazzes = fetchInterfacesImplemented( classesSet );
                    registration = context.registerService( clazzes, ref.get(), properties );
                }
            }
        }

        private String[] fetchInterfacesImplemented( Iterable<Type> classesSet )
        {
            String[] clazzes = new String[(int) Iterables.count( classesSet )];
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
