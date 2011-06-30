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
package org.qi4j.samples.dddsample.bootstrap;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration
@Ignore( "Bootstrapping in Spring doesn't work?? Also produces some GUI which won't work on the server until X Virtual Framebuffer has been installed." )
public final class BootstrapTest
{
    private static final String[] BEANS =
        {
            "trackingService",
            "bookingServiceFacade",
            "openUnitOfWorkInViewInterceptor"
        };

    @Autowired
    private ApplicationContext appContext;

    @Test
    public final void testQi4jServiceExportedToSpring()
    {
        for( String bean : BEANS )
        {
            assertNotNull( appContext.getBean( bean ) );
        }
    }
}
