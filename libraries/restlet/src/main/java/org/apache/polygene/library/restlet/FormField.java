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

package org.apache.polygene.library.restlet;

import java.util.Map;
import java.util.Objects;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;

@Mixins( FormField.Mixin.class )
public interface FormField extends HasName
{
    String TEXT = "TEXT";

    Property<String> type();

    @Optional
    @UseDefaults
    Property<String> value();

    Map.Entry<String,String> toMapEntry();

    abstract class Mixin
        implements FormField
    {
        @Override
        public Map.Entry<String, String> toMapEntry()
        {
            return new StringMapEntry( name().get(), value().get() );
        }
    }

    class StringMapEntry implements Map.Entry<String, String>
    {
        private final String key;
        private final String value;

        public StringMapEntry( String key, String value )
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public String getValue()
        {
            return value;
        }

        @Override
        public String setValue( String value )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( !( o instanceof StringMapEntry ) )
            {
                return false;
            }
            StringMapEntry miniMap = (StringMapEntry) o;
            return Objects.equals( key, miniMap.key ) &&
                   Objects.equals( value, miniMap.value );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( key, value );
        }
    }

}
