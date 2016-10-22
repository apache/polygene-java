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
package org.apache.zest.spi.entitystore.helpers;

import org.apache.zest.api.time.SystemTime;

/**
 * JSON keys for values in the stored data.
 */
public interface JSONKeys
{
    /**
     * Identity of the entity.
     */
    String IDENTITY = "identity";
    /**
     * Version of the application which last updated the entity.
     */
    String APPLICATION_VERSION = "application_version";
    /**
     * Type of the entity.
     */
    String TYPE = "type";
    /**
     * Version of the entity.
     */
    String VERSION = "version";
    /**
     * When entity was last modified according to {@link SystemTime#now()}
     */
    String MODIFIED = "modified";
    /**
     * Map of properties.
     */
    String PROPERTIES = "properties";
    /**
     * Map of associations.
     */
    String ASSOCIATIONS = "associations";
    /**
     * Map of manyassociations.
     */
    String MANY_ASSOCIATIONS = "manyassociations";
    /**
     * Map of namedassociations.
     */
    String NAMED_ASSOCIATIONS = "namedassociations";
}
