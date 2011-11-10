/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.rdf.repository;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * JAVADOC Add JavaDoc
 */
public interface HttpRepositoryConfiguration
    extends ConfigurationComposite
{
    /**
     * The URL of the remote Sesame HTTP Repository.
     *
     * Default: http://localhost:8183/
     *
     * @return The configured URL for the remote Sesame HTTP Repository.
     */
    @Optional
    Property<String> repositoryUrl();

    /**
     * The ID of the Repository at the remote Sesame HTTP host.
     *
     * Default: qi4j
     *
     * @return The configured ID at the remote Sesame HTTP host.
     */
    @Optional
    Property<String> repositoryId();
}
