package org.qi4j.library.osgi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Dictionary;
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
import org.qi4j.functional.Iterables;

import static org.qi4j.api.util.Classes.toClassName;
import static org.qi4j.api.util.Classes.typesOf;
import static org.qi4j.functional.Iterables.cast;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.toArray;

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
            for( ServiceReference ref : module.findServices( first( descriptor.types() ) ) )
            {
                if( ref.identity().equals( identity().get() ) )
                {
                    Iterable<Type> classesSet = cast(descriptor.types());
                    Dictionary properties = descriptor.metaInfo( Dictionary.class );
                    String[] clazzes = fetchInterfacesImplemented( classesSet );
                    registration = context.registerService( clazzes, ref.get(), properties );
                }
            }
        }

        private String[] fetchInterfacesImplemented( Iterable<Type> classesSet )
        {
            return toArray( String.class, map( toClassName(), typesOf( classesSet ) ) );
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
