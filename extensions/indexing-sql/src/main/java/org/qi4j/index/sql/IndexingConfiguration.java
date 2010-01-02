/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.index.sql;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

public interface IndexingConfiguration extends ConfigurationComposite
{
    /**
     *
     * Default: INSERT INTO QI_ASSOCIATIONS set ENTITY_ID = ?, set ASSOC_NAME = ?, set REF_ID = ?
     * 
     * @return the prepared SQL statement to use for updating the association table.
     */
    @Optional
    Property<String> insertAssociation();

    /**
     * Default: SELECT * FROM QI_ASSOCIATIONS
     * @return
     */
    @Optional
    Property<String> checkAssociationsTable();

    /**
     * Default:  CREATE TABLE QI_ASSOCIATIONS ENTITY_ID varchar(130), ASSOC_NAME varchar(250), REF_ID varchar(250)
     * @return
     */
    @Optional
    Property<String> createAssociationsTable();

    /**
     * Default: INSERT INTO QI_VALUES set VALUE_ID = ?, set VALUE_TYPE = ?, set VALUE_DATA = ?
     *
     * @return
     */
    @Optional
    Property<String> insertValue();

    /**
     * Default: SELECT * FROM QI_VALUES
     * @return
     */
    @Optional
    Property<String> checkValuesTable();

    /**
     * Default: CREATE TABLE QI_VALUES VALUE_ID varchar(130), VALUE_TYPE varchar(250), VALUE_DATA TEXT
     * @return
     */
    @Optional
    Property<String> createValuesTable();

    /**
     * Default: INSERT INTO QI_PROPERTIES set PROPERTY_ID = ?, set PROPERTY_NAME = ?, set PROPERTY_TYPE = ? , set PROPERTY_DATA = ?
     *
     * @return
     */
    @Optional
    Property<String> insertProperty();

    /**
     * Default: SELECT * FROM QI_PROPERTIES
     * @return
     */
    @Optional
    Property<String> checkPropertiesTable();

    /**
     * Default: CREATE TABLE QI_PROPERTIES PROPERTY_ID varchar(130), PROPERTY_NAME varchar(250), PROPERTY_TYPE varchar(250), PROPERTY_DATA TEXT
     * @return
     */
    @Optional
    Property<String> createPropertiesTable();
}
