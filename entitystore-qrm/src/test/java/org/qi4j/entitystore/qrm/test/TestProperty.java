/*
 * Copyright (c) 2008, Michael Hunger. All Rights Reserved.
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

package org.qi4j.entitystore.qrm.test;

import java.lang.reflect.Type;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;

public class TestProperty<T>
    implements Property<T>
{
    private final T value;
    private final QualifiedName name;

    public TestProperty( T value, QualifiedName name )
    {
        this.value = value;
        this.name = name;
    }

    public T get()
    {
        return value;
    }

    public void set( T newValue )
        throws IllegalArgumentException
    {
        set( newValue );
    }

    public T _()
    {
        return get();
    }

    public void _( T newValue )
        throws IllegalArgumentException, IllegalStateException
    {
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return null;
    }

    public boolean isImmutable()
    {
        return false;
    }

    public boolean isComputed()
    {
        return false;
    }

    public String name()
    {
        return name.name();
    }

    public QualifiedName qualifiedName()
    {
        return name;
    }

    public Type type()
    {
        return value.getClass();
    }
}
