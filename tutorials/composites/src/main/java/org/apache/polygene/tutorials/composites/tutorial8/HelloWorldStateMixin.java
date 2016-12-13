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
package org.apache.polygene.tutorials.composites.tutorial8;

import org.apache.polygene.api.injection.scope.State;
import org.apache.polygene.api.property.Property;

// START SNIPPET: solution

/**
 * This is the implementation of the HelloWorld
 * state interface.
 */
public class HelloWorldStateMixin
    implements HelloWorldState
{
    @State
    private Property<String> phrase;
    @State
    private Property<String> name;

    @Override
    public Property<String> phrase()
    {
        return phrase;
    }

    @Override
    public Property<String> name()
    {
        return name;
    }
}
// END SNIPPET: solution
