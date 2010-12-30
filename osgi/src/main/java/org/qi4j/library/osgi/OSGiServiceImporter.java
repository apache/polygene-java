/*
 * Copyright (c) 2010 Morgan Stanley & Co. Incorporated, All Rights Reserved
 *
 * Unpublished copyright.  All rights reserved.  This material contains
 * proprietary information that shall be used or copied only within
 * Morgan Stanley, except with written permission of Morgan Stanley.
 */
package org.qi4j.library.osgi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Dictionary;
import org.osgi.framework.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;

/**
 * Allows for importing OSGi services to Qi4j applications.
 * <p>
 * This allows for an OSGi service to be imported as a Qi4j service. Only one service instance can be handled at a
 * time.
 * </p>
 * <p>
 * Example; If you want to import the OSGi service that implements {@code YourOsgiServiceInterface}, you need
 * the following assembly;
 * </p>
 * <code><pre>
 *
 * public YourAssembler( BundleContext context )
 * {
 *     context = bundleContext;
 * }
 *
 * public void assemble( ModuleAssembly module )
 *     throws AssemblyException
 * {
 *     module.addServices( OSGiServiceImporter.class )
 *         .identifiedBy( "osgi" )
 *         .setMetaInfo( bundleContext );
 *     module.importServices( YourOsgiServiceInterface.class )
 *         .importedBy( ImportedServiceDeclaration.SERVICE_IMPORTER )
 *         .setMetaInfo("osgi");
 * }
 * </pre></code>
 * <p>
 * The string "osgi" is the binding between the Qi4j support for Imported Services and the OSGi service importer
 * implementation of this library. This string should be more unique, especially if there are a lot of OSGi services
 * to be imported, to avoid potential conflicts.
 * </p>
 * <p>
 * You can optionally provide a {@link FallbackStrategy} implementation to be invoked when the OSGi service instance
 * is not available, and a method call is invoked.
 * </p>
 * <p>
 * You can optionally provide a {@code org.osgi.framework.Filter} to further select the OSGi service you want to
 * use. Such as;
 * </p>
 * <code><pre>
 * Filter filter = new MySelectiveFilter();
 * module.addServices( OSGiServiceImporter.class )
 *     .identifiedBy( "osgi" )
 *     .setMetaInfo( bundleContext )
 *     .setMetaInfo( filter );
 * </pre></code>
 */
public class OSGiServiceImporter
    implements ServiceImporter, Activatable
{
    private ServiceFinder customizer;

    /**
     * Imports an instance of the service type described in the service descriptor.
     *
     * @param serviceDescriptor The service descriptor.
     * @return The imported service instance.
     * @throws org.qi4j.api.service.ServiceImporterException
     *          if import failed.
     */
    public Object importService(final ImportedServiceDescriptor serviceDescriptor)
        throws ServiceImporterException
    {
        Class serviceType = serviceDescriptor.type();
        BundleContext context = serviceDescriptor.metaInfo(BundleContext.class);
        if( context == null )
        {
            throw new ServiceImporterException(BundleContext.class.getName() + " must be set as meta info on the " + OSGiServiceImporter.class.getName() );
        }
        FallbackStrategy fallback = serviceDescriptor.metaInfo(FallbackStrategy.class);
        if( fallback == null)
        {
            fallback = new ExceptionFallback();
        }
        Filter filter = serviceDescriptor.metaInfo(Filter.class);
        if (filter == null) {
            filter = new TypeFilter(context, serviceType);
        }
        customizer = new ServiceFinder(context, filter, fallback);
        final Class[] intfaces = new Class[]{serviceType};
        return Proxy.newProxyInstance(getClass().getClassLoader(), intfaces, customizer);
    }

    public boolean isActive(final Object o)
    {
        return customizer.isAvailable();
    }

    public boolean isAvailable(final Object o)
    {
        return customizer.isAvailable();
    }

    @Override
    public void activate() throws Exception {
        customizer.open();
    }

    @Override
    public void passivate() throws Exception {
        customizer.close();
    }

    private static class ServiceFinder extends ServiceTracker
        implements InvocationHandler
    {
        private ServiceReference reference;
        private final FallbackStrategy fallback;

        ServiceFinder(BundleContext context, Filter filter, FallbackStrategy fallback)
        {
            super(context, filter, null);
            this.fallback = fallback;
        }

        boolean isAvailable()
        {
            return reference != null;
        }

        public Object addingService(ServiceReference reference)
        {
            this.reference = reference;
            return reference;
        }

        public void modifiedService(ServiceReference reference, Object service)
        {
        }

        public void removedService(ServiceReference reference, Object service)
        {
            this.reference = null;
        }

        public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable
        {
            ServiceReference invokeRef;
            Object service;
            synchronized (this) {
                invokeRef = reference;
                if (invokeRef == null)
                {
                    fallback.invoke(invokeRef, method, args);
                }
                service = context.getService(invokeRef);
                if (service == null)
                {
                    fallback.invoke(invokeRef, method, args);
                }
            }
            Object result = method.invoke(service, args);
            context.ungetService(invokeRef);
            return result;
        }
    }

    private static class TypeFilter
        implements Filter
    {
        private final BundleContext context;
        private final Class serviceType;

        public TypeFilter(BundleContext context, Class serviceType)
        {
            this.context = context;
            this.serviceType = serviceType;
        }

        /**
         * Filter using a service's properties.
         * <p/>
         * This <code>Filter</code> is executed using the keys and values of the
         * referenced service's properties. The keys are case insensitively matched
         * with this <code>Filter</code>.
         *
         * @param reference The reference to the service whose properties are used
         *                  in the match.
         * @return <code>true</code> if the service's properties match this
         *         <code>Filter</code>; <code>false</code> otherwise.
         */
        public boolean match(ServiceReference reference)
        {
            boolean sameClassSpace = reference.isAssignableTo(context.getBundle(), serviceType.getName());
            if (!sameClassSpace)
            {
                return false;
            }
            Object service = context.getService(reference);
            boolean isCorrectType = serviceType.isAssignableFrom(service.getClass());
            context.ungetService(reference);
            return isCorrectType;
        }

        /**
         * Filter using a <code>Dictionary</code>. This <code>Filter</code> is
         * executed using the specified <code>Dictionary</code>'s keys and values.
         * The keys are case insensitively matched with this <code>Filter</code>.
         *
         * @param dictionary The <code>Dictionary</code> whose keys are used in the
         *                   match.
         * @return <code>true</code> if the <code>Dictionary</code>'s keys and
         *         values match this filter; <code>false</code> otherwise.
         * @throws IllegalArgumentException If <code>dictionary</code> contains case
         *                                  variants of the same key name.
         */
        public boolean match(Dictionary dictionary)
        {
            return true;
        }

        /**
         * Filter with case sensitivity using a <code>Dictionary</code>. This
         * <code>Filter</code> is executed using the specified
         * <code>Dictionary</code>'s keys and values. The keys are case sensitively
         * matched with this <code>Filter</code>.
         *
         * @param dictionary The <code>Dictionary</code> whose keys are used in the
         *                   match.
         * @return <code>true</code> if the <code>Dictionary</code>'s keys and
         *         values match this filter; <code>false</code> otherwise.
         * @since 1.3
         */
        public boolean matchCase(Dictionary dictionary)
        {
            return true;
        }

        @Override
        public String toString()
        {
            return "(objectClass=" + serviceType.getName() + ")";
        }
    }

    private static class ExceptionFallback
        implements FallbackStrategy
    {
        @Override
        public Object invoke(final ServiceReference reference, final Method method, final Object... args)
        {
            throw new ServiceImporterException("Service not available at the moment: " + reference);
        }
    }
}