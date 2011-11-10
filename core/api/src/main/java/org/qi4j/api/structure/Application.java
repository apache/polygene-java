/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.structure;

import org.qi4j.api.event.ActivationEventListenerRegistration;
import org.qi4j.api.service.Activatable;

/**
 * The Application represents a whole Qi4j application.
 */
public interface Application
    extends ActivationEventListenerRegistration, Activatable
{
    public enum Mode
    {
        // Application modes
        test,           // Should be used for unit test runs. Created files etc. should be cleaned up between runs
        development,    // Should be used during development. Typically create in-memory databases etc.
        staging,        // Should be used in QA environments, and other production-like settings where different set of external resources are utilized.
        production      // Should be used in production. All databases are persistent on disk etc.
    }

    String name();

    /**
     * The version of the application. This can be in any format, but
     * most likely will follow the Dewey format, i.e. x.y.z.
     *
     * @return the version of the application
     */
    String version();

    Mode mode();

    <T> T metaInfo( Class<T> infoType );

    Layer findLayer( String layerName )
        throws IllegalArgumentException;

    Module findModule( String layerName, String moduleName )
        throws IllegalArgumentException;

    ApplicationDescriptor descriptor();
}
