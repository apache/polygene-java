/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.structure;
/**
 *  TODO
 */

import org.qi4j.test.AbstractQi4jTest;

public class ApplicationBuilderHelperTest extends AbstractQi4jTest
{
    public void testNewSimpleApplication() throws Exception
    {
        Assembly assembly = new SimpleAssembly();
        applicationBuilderHelper = new ApplicationBuilderHelper( api.getApplicationBuilderFactory() );
        Application app = applicationBuilderHelper.newApplication( assembly );

        Layer layer = app.getLayers().iterator().next();
        Module module = layer.getModules().iterator().next();
        Assembly appAssembly = module.getAssemblies().iterator().next();
        assertEquals( assembly, appAssembly );
    }

    static class SimpleAssembly
        extends AbstractAssembly
    {

    }
}