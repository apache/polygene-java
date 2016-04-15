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

package org.apache.zest.runtime.mixin;

import org.junit.Test;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.mixin.NoopMixin;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.junit.Assert.fail;

/**
 * @author Stanislav Muhametsin
 */
public class InvokeServiceFromModuleAssemblyTest
    extends AbstractZestTest
{
    @Mixins( NoopMixin.class )
    public interface TestService
        extends ServiceComposite
    {
        public void voidMethod();
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        try
        {
            module.services( TestService.class );
            module.forMixin( TestService.class ).declareDefaults().voidMethod();
            fail( "It is not allowed to declareDefaults on methods not returning Property." );
        }
        catch( IllegalArgumentException e )
        {
            // expected
        }
    }

    @Test
    public void failingAssemblyTest()
    {

    }
}
