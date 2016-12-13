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
package org.apache.polygene.library.sql.datasource;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.configuration.Enabled;
import org.apache.polygene.api.property.Property;

/**
 * Describe DataSourceConfiguration properties.
 */
// START SNIPPET: config
public interface DataSourceConfigurationState
        extends Enabled
{
    Property<String> driver();
    Property<String> url();
    @UseDefaults Property<String> username();
    @UseDefaults Property<String> password();
    @Optional Property<Integer> minPoolSize();
    @Optional Property<Integer> maxPoolSize();
    @Optional Property<Integer> loginTimeoutSeconds();
    @Optional Property<Integer> maxConnectionAgeSeconds();
    @Optional Property<String> validationQuery();
    @UseDefaults Property<String> properties();
}
// END SNIPPET: config
