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
package org.qi4j.tutorials.composites.tutorial9;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

/**
 * This interface contains only the state
 * of the HelloWorld object.
 * <p>
 * The state is declared using Properties. The @NotEmpty annotation is applied to the
 * method to check that the properties are not set to empty strings.
 * </p>
 */
public interface HelloWorldState
{
    @NotEmpty
    Property<String> phrase();

    @NotEmpty
    Property<String> name();
}
