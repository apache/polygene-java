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

package org.apache.polygene.runtime.injection;

import org.apache.polygene.bootstrap.InvalidInjectionException;
import org.apache.polygene.runtime.model.Resolution;

/**
 * JAVADOC
 */
public interface InjectionProviderFactory
{
    /**
     * Binding a dependency given an injection resolution. If no binding
     * can be found, return null. If the dependency is optional the dependency will
     * then be explicitly set to null.
     *
     * @param resolution Injection resolution
     * @param dependencyModel Dependency model
     * @return InjectionProvider
     * @throws InvalidInjectionException if the injection is invalid
     */
    InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException;
}
