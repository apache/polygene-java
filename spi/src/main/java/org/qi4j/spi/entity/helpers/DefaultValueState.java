/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity.helpers;

import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.value.ValueState;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Default implementation of ValueState.
 */
public final class DefaultValueState
    implements ValueState, Serializable
{
    final Map<StateName, String> values;

    public DefaultValueState()
    {
        this(new HashMap<StateName, String>());
    }

    public DefaultValueState( Map<StateName, String> values )
    {
        this.values = values;
    }

    public String getProperty( StateName stateName )
    {
        return values.get( stateName );
    }

    public Map<StateName, String> values()
    {
        return values;
    }

    @Override public String toString()
    {
        return values.toString();
    }
}
