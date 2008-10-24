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

import org.ops4j.pax.drone.api.BundleProvision;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class BundleLifecycleTest extends AbstractTest
{

    @Override
    protected final BundleProvision newBundleProvision()
    {
        BundleProvision bundleProvision = super.newBundleProvision();
        if( "testLifecycleWithCglibInstalled".equals( getName() ) )
        {
            bundleProvision.addBundle( "mvn:net.sourceforge.cglib/com.springsource.net.sf.cglib/2.1.3" );
        }
        return bundleProvision;
    }

    public final void testLifecycleWithCglibInstalled()
        throws BundleException
    {
        testLifecycle();
    }

    public final void testLifecycle()
        throws BundleException
    {
        assertNotNull( getModuleServiceRef() );

        Bundle exampleBundle = getQi4jExampleBundle();
        exampleBundle.stop();

        assertNull( getModuleServiceRef() );

        exampleBundle.start();

        assertNotNull( getModuleServiceRef() );
    }
}
