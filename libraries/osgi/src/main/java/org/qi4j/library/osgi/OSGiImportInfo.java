package org.qi4j.library.osgi;

import org.osgi.framework.BundleContext;

/**
 * MetaInfo to define imported OSGi services.
 */
public class OSGiImportInfo
{
    private final BundleContext context;
    private final Class[] intfaces;

    public OSGiImportInfo( BundleContext context, Class... intfaces )
    {
        this.context = context;
        this.intfaces = intfaces;
    }

    public BundleContext bundleContext()
    {
        return context;
    }

    public Class[] interfaces()
    {
        return intfaces;
    }
}
