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

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

/**
 * The SimpleAlarmCategory only defines an optional Description property, and is provided more as an example
 * of how {@link AlarmCategory} may be extended.
 *
 * Note that this {@link AlarmCategory} is registered in the default {@link org.qi4j.bootstrap.Assembler} with a
 * Layer {@link org.qi4j.api.common.Visibility}, so if you subtype this, it is likely you need to create your own
 * {@link org.qi4j.bootstrap.Assembler}
 */
public interface SimpleAlarmCategory
    extends AlarmCategory
{

    @Optional
    Property<String> description();
}
