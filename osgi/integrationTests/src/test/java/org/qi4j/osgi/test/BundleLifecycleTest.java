/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.osgi.test;

import org.ops4j.pax.drone.api.DroneConnector;
import org.ops4j.pax.drone.connector.paxrunner.PaxRunnerConnectorFactory;
import org.ops4j.pax.drone.connector.paxrunner.Platforms;
import org.ops4j.pax.drone.spi.junit.DroneTestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.qi4j.core.test.osgi.Simple;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class BundleLifecycleTest extends DroneTestCase
{
    private static final String SYMBOLIC_NAME_QI4J_EXAMPLE = "org.qi4j.core.osgi.qi4j-osgi-example";

    @Override
    public final DroneConnector configure()
    {
        return PaxRunnerConnectorFactory.create( this )
            .setPlatform( Platforms.EQUINOX )
            .addBundle( "mvn:net.sourceforge.cglib/com.springsource.net.sf.cglib/2.1.3" )
            .addBundle( "mvn:org.ops4j.pax.logging/pax-logging-api" )
            .addBundle( "mvn:org.ops4j.pax.logging/pax-logging-service" )
            .addBundle( "mvn:org.qi4j.core/qi4j-core-api" )
            .addBundle( "mvn:org.qi4j.core/qi4j-core-spi" )
            .addBundle( "mvn:org.qi4j.core/qi4j-core-runtime" )
            .addBundle( "mvn:org.qi4j.core/qi4j-core-bootstrap" )
            .addBundle( "mvn:org.qi4j.core.osgi/qi4j-osgi-example" )
            .addBundle( "mvn:org.ops4j.pax.swissbox/pax-swissbox-extender" )
            .addBundle( "mvn:org.ops4j.pax.swissbox/pax-swissbox-core" )
            .addBundle( "mvn:org.ops4j.base/ops4j-base-lang" )
            .addBundle( "mvn:org.ops4j.pax.swissbox/pax-swissbox-lifecycle" );
    }

    public final void testLifecycle()
        throws BundleException
    {
        assertNotNull( getSimpleServiceRef() );

        Bundle exampleBundle = getQi4jExampleBundle();
        exampleBundle.stop();

        assertNull( getSimpleServiceRef() );

        exampleBundle.start();

        assertNotNull( getSimpleServiceRef() );
    }

    private ServiceReference getSimpleServiceRef()
    {
        BundleContext bundleContext = droneContext.getBundleContext();
        return bundleContext.getServiceReference( Simple.class.getName() );
    }

    private Bundle getQi4jExampleBundle()
    {
        Bundle exampleBundle = null;
        BundleContext context = droneContext.getBundleContext();
        Bundle[] bundles = context.getBundles();
        for( Bundle bundle : bundles )
        {

            String symbolicName = bundle.getSymbolicName();
            if( SYMBOLIC_NAME_QI4J_EXAMPLE.equals( symbolicName ) )
            {
                exampleBundle = bundle;
                break;
            }
        }
        assertNotNull( exampleBundle );
        return exampleBundle;
    }
}
