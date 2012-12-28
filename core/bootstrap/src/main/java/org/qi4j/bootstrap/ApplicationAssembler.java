/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.bootstrap;

/**
 * Implement this interface to create the root class that
 * is responsible for assembling your entire application.
 *
 * Model introspectors will instantiate this class and call assemble
 * to create the application, which will then be visited to get
 * information about its structure.
 *
 * Application deployment servers will instantiate this, call assemble,
 * and then activate the created application, which will be the runtime
 * instance that forms your application.
 */
public interface ApplicationAssembler
{
    ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException;
}
