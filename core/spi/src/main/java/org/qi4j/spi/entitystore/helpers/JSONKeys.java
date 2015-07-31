/*
 * Copyright (c) 2009-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.spi.entitystore.helpers;

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
     * When entity was last modified according to System.currentTimeMillis().
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
