/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.jclouds;

import java.util.Map;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Configuration of JCloudsMapEntityStore service.
 */
public interface JCloudsMapEntityStoreConfiguration
        extends ConfigurationComposite
{
    // START SNIPPET: config
    /**
     * Name of the JClouds provider to use. Defaults to 'transient'.
     */
    @Optional Property<String> provider();
    @UseDefaults Property<String> identifier();
    @UseDefaults Property<String> credential();
    /**
     * Use this to fine tune your provider implementation according to JClouds documentation.
     */
    @UseDefaults Property<Map<String, String>> properties();
    /**
     * Name of the JClouds container to use. Defaults to 'qi4j-entities'.
     */
    @Optional Property<String> container();
    // END SNIPPET: config

}
