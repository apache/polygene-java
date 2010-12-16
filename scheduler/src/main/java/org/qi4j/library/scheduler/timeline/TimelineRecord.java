/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.scheduler.timeline;

import java.util.List;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

/**
 * @author Paul Merlin
 */
public interface TimelineRecord
{

    Property<Long> timestamp();

    Property<SchedulerEvent> event();

    Property<String> taskName();

    @UseDefaults
    Property<List<String>> taskTags();

    @Queryable( false )
    @UseDefaults
    Property<String> details();

}
