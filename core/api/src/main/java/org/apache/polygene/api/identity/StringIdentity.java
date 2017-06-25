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
package org.apache.polygene.api.identity;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringIdentity
    implements Identity
{
    private final String value;

    private StringIdentity(String value)
    {
        Objects.requireNonNull( value, "Identity can not be null." );
        this.value = value;
    }

    public StringIdentity(byte[] bytes)
    {
        value = new String(bytes, StandardCharsets.UTF_8);
    }

    public String value()
    {
        return value;
    }

    @Override
    public byte[] toBytes()
    {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString()
    {
        return value;
    }

    public static Identity identityOf( String serializedState )
    {
        return new StringIdentity( serializedState );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        StringIdentity that = (StringIdentity) o;

        return value.equals(that.value);

    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
