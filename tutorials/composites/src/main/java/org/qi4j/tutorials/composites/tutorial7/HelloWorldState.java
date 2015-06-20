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
package org.qi4j.tutorials.composites.tutorial7;

import org.qi4j.library.constraints.annotation.NotEmpty;

// START SNIPPET: solution

/**
 * This interface contains only the state
 * of the HelloWorld object.
 * <p>
 * The parameters are declared as @NotEmpty, so the client cannot pass in empty strings
 * as values.
 * </p>
 */
public interface HelloWorldState
{
    void setPhrase( @NotEmpty String phrase )
        throws IllegalArgumentException;

    String getPhrase();

    void setName( @NotEmpty String name )
        throws IllegalArgumentException;

    String getName();
}
// END SNIPPET: solution
