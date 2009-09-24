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

/**
 * The Application represents a whole Qi4j application.
 */
public interface Application
{
    public enum Mode
    {
        // Application modes
        test, development, staging, production
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

    Layer findLayer( String layerName );

    Module findModule( String layerName, String moduleName );
}
