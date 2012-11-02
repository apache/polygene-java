/*
 * Copyright (c) 2010 Morgan Stanley & Co. Incorporated, All Rights Reserved
 *
 * Unpublished copyright.  All rights reserved.  This material contains
 * proprietary information that shall be used or copied only within
 * Morgan Stanley, except with written permission of Morgan Stanley.
 */
package org.qi4j.library.osgi;


import java.lang.reflect.Method;
import org.osgi.framework.ServiceReference;

/**
 * The fallback strategy is invoked when the OSGi service is not available and a method call is invoked.
 * <p>
 * The FallbackStrategy is declared on the {@link OSGiServiceImporter} service declaration, like;
 * <code><pre>
 *     FallbackStrategy strategy = new MyStrategy();
 *     module.services( OSGiServiceImporter.class )
 *         .identifiedBy( "osgi" )
 *         .setMetaInfo( bundleContext )
 *         .setMetaInfo( strategy );
 * </pre></code>
 */
public interface FallbackStrategy {
    Object invoke(final ServiceReference reference, Method method, Object... args);
}