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

package org.qi4j.spi.property;

import java.util.List;
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.structure.Module;

/**
 * Base class for types of values in ValueComposites.
 */
public interface ValueType
{
    TypeName type();

    boolean isNumber();

    boolean isBoolean();

    boolean isString();

    boolean isValue();

    boolean isDate();

    boolean isEnum();

    List<PropertyType> types();

    void toJSON( Object value, JSONWriter json )
        throws JSONException;

    Object toJSON( Object value )
        throws JSONException;

    Object fromJSON( Object object, Module module )
        throws JSONException;

    String toQueryParameter( Object value )
        throws IllegalArgumentException;

    Object fromQueryParameter( String parameter, Module module )
        throws IllegalArgumentException, JSONException;
}
