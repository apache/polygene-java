/*
 * Copyright (c) 2011, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.alarm;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * AlarmCategory is intended to be sub-interfaced, to create an explicit type for the domain where it is used.
 * <p>
 * AlarmCategory is used to provide a categorization of Alarms for grouping purposes, which in turn can drive
 * {@link AlarmEvent} forwarding routed by category. Category can be extended to contain additional state, which
 * will be associated with the AlarmPoint and related events.
 * </p>
 */
public interface AlarmCategory extends ValueComposite
{
    Property<String> name();
}
