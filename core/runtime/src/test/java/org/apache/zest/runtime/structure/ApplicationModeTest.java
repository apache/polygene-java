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

package org.apache.zest.runtime.structure;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.structure.Application;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.test.AbstractZestTest;

public class ApplicationModeTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {

    }

    @Test
    public void testApplicationModeNotSet()
    {
        // mode is set to test in AbstractZestTest
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
