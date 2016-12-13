/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.bootstrap;

import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ServiceAssemblyTest extends AbstractPolygeneTest
{
    @Test
    public void givenMyServiceWithTwoDeclarationsWhenActivatingServiceExpectServiceActivatedOnce()
        throws Exception
    {
        ServiceReference<MyService> ref = serviceFinder.findService( MyService.class );
        MyService underTest = ref.get();
        assertThat(underTest.activated(), equalTo(1));
        underTest.passivateService();
        assertThat(underTest.passivated(), equalTo(1));
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MyService.class ).instantiateOnStartup();
        module.services( MyService.class ).setMetaInfo( "Hello" );
    }

    @Mixins( MyServiceMixin.class )
    public static interface MyService extends ServiceActivation
    {
        int activated();
        int passivated();
    }

    public static class MyServiceMixin implements MyService, ServiceActivation
    {

        private int activated;
        private int passivated;

        @Override
        public int activated()
        {
            return activated;
        }

        @Override
        public int passivated()
        {
            return passivated;
        }

        @Override
        public void activateService()
            throws Exception
        {
            activated++;
        }

        @Override
        public void passivateService()
            throws Exception
        {
            passivated++;

        }
    }
}
