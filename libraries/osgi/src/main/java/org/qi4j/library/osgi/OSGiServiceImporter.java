package org.qi4j.library.osgi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceUnavailableException;

/**
 * Import OSGi services in a Qi4j Module.
 */
public class OSGiServiceImporter
    implements ServiceImporter
{

    @Override
    public Object importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        OSGiImportInfo info = serviceDescriptor.metaInfo( OSGiImportInfo.class );
        BundleContext context = info.bundleContext();
        ServiceTrackerHandler handler = new ServiceTrackerHandler( context, serviceDescriptor.type() );
        handler.open();
        return Proxy.newProxyInstance( serviceDescriptor.type().getClassLoader(), info.interfaces(), handler );
    }

    @Override
    public boolean isAvailable( Object instance )
    {
        ServiceTrackerHandler handler = (ServiceTrackerHandler) Proxy.getInvocationHandler( instance );
        return handler.isAvailable();
    }

    public static class ServiceTrackerHandler
        extends ServiceTracker
        implements InvocationHandler
    {

        private volatile Object service;
        private Class typeToImport;

        public ServiceTrackerHandler( BundleContext context, Class typeToImport )
        {
            super( context, typeToImport.getName(), null );
            this.typeToImport = typeToImport;
        }

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            Object temporary;
            synchronized( this )
            {
                if( service == null )
                {
                    wait( 5000 );  // Max wait 5 seconds.
                    if( service == null )
                    {
                        throw new ServiceUnavailableException(
                            "The OSGi service is currently not available: " + typeToImport.getName() );
                    }
                }
                temporary = service;
            }
            return method.invoke( temporary, args );
        }

        @Override
        public Object addingService( ServiceReference reference )
        {
            synchronized( this )
            {
                service = super.addingService( reference );
                notifyAll();
                return service;
            }
        }

        @Override
        public void removedService( ServiceReference reference, Object service )
        {
            synchronized( this )
            {
                super.removedService( reference, service );
                notifyAll();
                this.service = null;
            }
        }

        public boolean isAvailable()
        {
            return service != null;
        }

    }

}
