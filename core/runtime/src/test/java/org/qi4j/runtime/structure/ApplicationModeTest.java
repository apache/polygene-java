/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2009, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.structure;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.test.AbstractQi4jTest;

public class ApplicationModeTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {

    }

    @Test
    public void testApplicationModeNotSet()
    {
        // mode is set to test in AbstractQi4jTest
        Assert.assertThat( "mode set to default",
                           application.mode(),
                           CoreMatchers.equalTo( Application.Mode.test ) );
    }

    @Test
    public void testApplicationTestModeSet()
        throws ActivationException, AssemblyException
    {
        Application.Mode mode = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.layer().application().setMode( Application.Mode.test );
            }
        }.application().mode();

        Assert.assertThat( "mode set to given value", mode, CoreMatchers.equalTo( Application.Mode.test ) );
    }

    @Test
    public void testApplicationStagingModeSet()
        throws ActivationException, AssemblyException
    {
        Application.Mode mode = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.layer().application().setMode( Application.Mode.staging );
            }
        }.application().mode();

        Assert.assertThat( "mode set to given value", mode, CoreMatchers.equalTo( Application.Mode.staging ) );
    }

    @Test
    public void testApplicationDevelopmentModeSet()
        throws ActivationException, AssemblyException
    {
        Application.Mode mode = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.layer().application().setMode( Application.Mode.development );
            }
        }.application().mode();

        Assert.assertThat( "mode set to given value", mode, CoreMatchers.equalTo( Application.Mode.development ) );
    }

    @Test
    public void testApplicationProductionModeSet()
        throws ActivationException, AssemblyException
    {
        Application.Mode mode = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.layer().application().setMode( Application.Mode.production );
            }
        }.application().mode();

        Assert.assertThat( "mode set to given value", mode, CoreMatchers.equalTo( Application.Mode.production ) );
    }
}
