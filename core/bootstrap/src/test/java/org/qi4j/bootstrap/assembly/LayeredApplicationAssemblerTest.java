/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.bootstrap.assembly;

import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class LayeredApplicationAssemblerTest
{
    @Test
    public void validateThatAssemblerCreatesApplication()
        throws AssemblyException, ActivationException
    {
        TestApplication assembler = new TestApplication( "Test Application", "1.0.1", Application.Mode.test );
        assembler.start();

        assertThat( assembler.application().name(), equalTo("Test Application") );
        assertThat( assembler.application().version(), equalTo("1.0.1") );
    }
}
