/*
 * Copyright (c) 2010 Morgan Stanley & Co. Incorporated, All Rights Reserved
 *
 * Unpublished copyright.  All rights reserved.  This material contains
 * proprietary information that shall be used or copied only within
 * Morgan Stanley, except with written permission of Morgan Stanley.
 */
package com.pathfinder.internal;

import com.pathfinder.api.GraphTraversalService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class Activator
        implements BundleActivator {
    private ServiceRegistration registration;

    public void start(final BundleContext bundleContext) throws Exception {
        GraphDAO dao = new GraphDAO();
        GraphTraversalServiceImpl service = new GraphTraversalServiceImpl(dao);
        registration = bundleContext.registerService(GraphTraversalService.class.getName(), service, null);
    }

    public void stop(final BundleContext bundleContext) throws Exception {
        registration.unregister();
    }
}