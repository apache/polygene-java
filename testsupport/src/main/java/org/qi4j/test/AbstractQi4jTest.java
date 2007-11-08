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

package org.qi4j.test;

import junit.framework.TestCase;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.CompositeRegistry;
import org.qi4j.Qi4j;
import org.qi4j.runtime.Energy4Java;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.structure.ApplicationBuilderHelper;

/**
 * Base class for Composite tests
 */
public abstract class AbstractQi4jTest
    extends TestCase
{
    protected Qi4j api;
    protected Qi4jSPI spi;
    protected Qi4jRuntime runtime;

    protected ApplicationBuilderHelper applicationBuilderHelper;

    protected CompositeBuilderFactory factory;
    protected CompositeRegistry registry;

    @Override protected void setUp() throws Exception
    {
        api = spi = runtime = new Energy4Java();
        factory = runtime.newCompositeBuilderFactory();
        registry = runtime.getCompositeRegistry();
        applicationBuilderHelper = new ApplicationBuilderHelper( api.getApplicationBuilderFactory() );
    }
}
