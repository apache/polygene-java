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

package org.apache.zest.spi.value;

import java.lang.reflect.Type;
import java.util.List;

public class BooleanClassDeserializer
    implements TypeDeserializer<Boolean>
{

    @Override
    public Boolean deserialize( Class<? extends Boolean> type, VDA parent, Object parser )
        throws Exception
    {
        Object value = parent.nextValue( parser );
        if( value instanceof String )
        {
            return Boolean.valueOf( (String) value );
        }
        if( value instanceof Boolean )
        {
            return (Boolean) value;
        }
        throw new IllegalDeserializationException( "Boolean value expected.", parent.location(parser) );
    }

    @Override
    public Boolean deserialize( Class<? extends Boolean> type, VDA parent, String stringValue )
        throws Exception
    {
        return Boolean.valueOf( stringValue );
    }

    @Override
    public List<String> fieldNames( Class<?> type )
    {
        return null;
    }

    @Override
    public Class<?> fieldTypeOf( Type parent, String fieldName )
    {
        return null;
    }

    @Override
    public Boolean createObject( Object data )
    {
        return null;
    }
}
