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
package org.apache.zest.sample.forum.domainevent;

import java.util.List;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueComposite;

/**
 * TODO
 */
public interface DomainEventValue
    extends ValueComposite
{
    // Version of the application that created these events
    Property<String> version();

    // When the event occurred
    Property<Long> timestamp();

    // Selected objects
    @UseDefaults
    Property<List<String>> selection();

    // Type of the entity being invoked
    Property<String> context();

    // Name of method/event
    Property<String> name();

    // Method parameters as JSON
    @UseDefaults
    Property<List<ParameterValue>> parameters();
}
