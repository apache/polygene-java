/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.qi4j.bootstrap;

import org.qi4j.api.common.Visibility;

/**
 * Fluent API for declaring configurations. Instances
 * of this API are acquired by calling {@link ModuleAssembly#configurations(Class[])}.
 */
public interface ConfigurationDeclaration
{
    /**
     * Set additional metainfo for this configuration declaration.
     *
     * @param info metainfo that can be retrieved from the CompositeDescriptor.
     *
     * @return This instance for a fluid DSL
     */
    ConfigurationDeclaration setMetaInfo( Object info );

    /**
     * Set visibility for declared entities.
     *
     * @param visibility The {@link Visibility} that this ConfigurationComposite will have.
     *
     * @return This instance for a fluid DSL
     */
    ConfigurationDeclaration visibleIn( Visibility visibility );

    /**
     * Declare additional concerns for these configurations.
     *
     * @param concerns The concerns that are to be added to the ConfigurationComposite beyond the statically declared ones.
     *
     * @return This instance for a fluid DSL
     */
    ConfigurationDeclaration withConcerns( Class<?>... concerns );

    /**
     * Declare additional side-effects for these configurations.
     *
     * @param sideEffects The sideeffects that are to be added to the ConfigurationComposite beyond the statically declared ones.
     *
     * @return This instance for a fluid DSL
     */
    ConfigurationDeclaration withSideEffects( Class<?>... sideEffects );

    /**
     * Declare additional mixins for these configurations.
     * <p>
     * This can be useful to override any default mixins from the configuration interface.
     * </p>
     * @param mixins The mixins that are to be added to the ConfigurationComposite beyond the statically declared ones.
     *
     * @return This instance for a fluid DSL
     */
    ConfigurationDeclaration withMixins( Class<?>... mixins );

    /**
     * Declare additional interfaces for these declared interfaces.
     * <p>
     * This can be useful to add types that the Configuration should implement, but
     * which you do not want to include in the entity interface itself.
     * </p>
     * @param types list of interfaces to add
     *
     * @return This instance for a fluid DSL
     */
    ConfigurationDeclaration withTypes( Class<?>... types );
}
