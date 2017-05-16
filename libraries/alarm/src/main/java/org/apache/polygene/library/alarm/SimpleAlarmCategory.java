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

package org.apache.polygene.library.alarm;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;

/**
 * The SimpleAlarmCategory only defines an optional Description property, and is provided more as an example
 * of how {@link AlarmCategory} may be extended.
 *
 * Note that this {@link AlarmCategory} is registered in the default {@link org.apache.polygene.bootstrap.Assembler} with a
 * Layer {@link org.apache.polygene.api.common.Visibility}, so if you subtype this, it is likely you need to create your own
 * {@link org.apache.polygene.bootstrap.Assembler}
 */
public interface SimpleAlarmCategory
    extends AlarmCategory
{

    @Optional
    Property<String> description();
}
