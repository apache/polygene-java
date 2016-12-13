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
package org.apache.zest.api.structure;

import java.util.stream.Stream;
import org.apache.zest.api.activation.Activation;
import org.apache.zest.api.activation.ActivationEventListenerRegistration;

/**
 * The Application represents a whole Polygene application.
 */
public interface Application
    extends ActivationEventListenerRegistration, Activation, MetaInfoHolder
{
    /**
     * Application modes.
     */
    public enum Mode
    {
        /**
         * Should be used for unit test runs. Created files etc. should be cleaned up between runs.
         */
        test,
        /**
         * Should be used during development. Typically create in-memory databases etc.
         */
        development,
        /**
         * Should be used in QA environments, and other production-like settings where different set of external
         * resources are utilized.
         */
        staging,
        /**
         * Should be used in production. All databases are persistent on disk etc.
         */
        production
    }

    /**
     * @return Application name
     */
    String name();

    /**
     * The version of the application. This can be in any format, but
     * most likely will follow the Dewey format, i.e. x.y.z.
     *
     * @return the version of the application
     */
    String version();

    /**
     * @return Application Mode
     */
    Mode mode();

    /**
     * Find a Layer.
     *
     * @param layerName Layer name
     * @return Found Layer, never returns null
     * @throws IllegalArgumentException if there's no such Layer
     */
    Layer findLayer( String layerName )
        throws IllegalArgumentException;

    /**
     * Find a Module.
     *
     * @param layerName Layer name
     * @param moduleName Module name
     * @return Found Module, never returns null
     * @throws IllegalArgumentException if there's no such Module
     */
    Module findModule( String layerName, String moduleName )
        throws IllegalArgumentException;

    /**
     * @return Application Descriptor
     */
    ApplicationDescriptor descriptor();

    Stream<? extends Layer> layers();
}
