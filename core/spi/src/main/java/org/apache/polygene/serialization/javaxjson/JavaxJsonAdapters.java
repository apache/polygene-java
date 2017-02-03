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
 */
package org.apache.polygene.serialization.javaxjson;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.type.ValueType;

import static org.apache.polygene.api.type.HasTypesCollectors.closestType;

@Mixins( JavaxJsonAdapters.Mixin.class )
public interface JavaxJsonAdapters
{
    void registerAdapter( ValueType valueType, JavaxJsonAdapter<?> adapter );

    <T> JavaxJsonAdapter<T> adapterFor( ValueType valueType );

    default <T> JavaxJsonAdapter<T> adapterFor( Class<T> type )
    {
        return adapterFor( ValueType.of( type ) );
    }

    class Mixin implements JavaxJsonAdapters
    {
        private Map<ValueType, JavaxJsonAdapter<?>> adapters = new LinkedHashMap<>();

        @Override
        public void registerAdapter( ValueType valueType, JavaxJsonAdapter<?> adapter )
        {
            adapters.put( valueType, adapter );
        }

        @Override
        public <T> JavaxJsonAdapter<T> adapterFor( ValueType valueType )
        {
            return castAdapter( adapters.keySet().stream()
                                        .collect( closestType( valueType ) )
                                        .map( adapters::get )
                                        .orElse( null ) );
        }

        @SuppressWarnings( "unchecked" )
        private <T> JavaxJsonAdapter<T> castAdapter( JavaxJsonAdapter<?> adapter )
        {
            return (JavaxJsonAdapter<T>) adapter;
        }
    }
}
