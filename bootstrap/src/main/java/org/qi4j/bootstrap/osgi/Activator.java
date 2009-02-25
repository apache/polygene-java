/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.bootstrap.osgi;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.ops4j.pax.swissbox.core.BundleClassLoader;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.ops4j.pax.swissbox.extender.BundleWatcher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.internal.ServiceLoader;

public final class Activator
    implements BundleActivator
{
    private BundleWatcher<URL> urlBundleWatcher;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        BundleURLScanner urlScanner =
            new BundleURLScanner( "META-INF/services", ApplicationAssemblyFactory.class.getName(), false );
        urlBundleWatcher = new BundleWatcher<URL>( bundleContext, urlScanner, new Qi4jBundleObserver() );
        urlBundleWatcher.start();
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        urlBundleWatcher.stop();
    }

    private static class Qi4jBundleObserver
        implements BundleObserver<URL>
    {
        private final HashMap<Bundle, ClassLoader> loaders;

        private Qi4jBundleObserver()
        {
            loaders = new HashMap<Bundle, ClassLoader>();
        }

        public void addingEntries( Bundle bundle, List<URL> entries )
        {
            BundleClassLoader classloader = new BundleClassLoader( bundle );
            ServiceLoader.addClassloader( classloader );
            loaders.put( bundle, classloader );
        }

        public void removingEntries( Bundle bundle, List<URL> entries )
        {
            ClassLoader classloader = loaders.get( bundle );
            ServiceLoader.removeClassloader( classloader );
        }
    }
}
