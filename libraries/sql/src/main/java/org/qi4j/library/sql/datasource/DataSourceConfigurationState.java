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
package org.qi4j.library.sql.datasource;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * Describe DataSourceConfiguration properties.
 */
public interface DataSourceConfigurationState
        extends Enabled
{

    Property<String> driver();

    Property<String> url();

    Property<String> username();

    Property<String> password();

    @UseDefaults
    Property<String> properties();

}
