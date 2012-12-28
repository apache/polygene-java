package org.qi4j.runtime.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.bootstrap.RuntimeFactory;
import org.qi4j.runtime.Qi4jRuntimeImpl;

/**
 *
 */
public class Activator
    implements BundleActivator
{
    private ServiceRegistration registration;

    @Override
    public void start( BundleContext bundleContext )
        throws Exception
    {
        RuntimeFactory factory = new RuntimeFactory()
        {
            @Override
            public Qi4jRuntime createRuntime()
            {
                return new Qi4jRuntimeImpl();
            }
        };
        registration = bundleContext.registerService( RuntimeFactory.class.getName(), factory, null );
    }

    @Override
    public void stop( BundleContext bundleContext )
        throws Exception
    {
        registration.unregister();
    }
}
