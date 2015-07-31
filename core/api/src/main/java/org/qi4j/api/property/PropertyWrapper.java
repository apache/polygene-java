/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.api.property;

/**
 * If you want to catch getting and setting properties, then create a GenericConcern
 * that wraps the Zest-supplied Property instance with PropertyWrappers. Override
 * get() and/or set() to perform your custom code.
 */
public class PropertyWrapper
    implements Property<Object>
{
    protected Property<Object> next;

    public PropertyWrapper( Property<Object> next )
    {
        this.next = next;
    }

    public Property<Object> next()
    {
        return next;
    }

    @Override
    public Object get()
    {
        return next.get();
    }

    @Override
    public void set( Object newValue )
        throws IllegalArgumentException, IllegalStateException
    {
        next.set( newValue );
    }

    @Override
    public int hashCode()
    {
        return next.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return next.equals( obj );
    }

    @Override
    public String toString()
    {
        return next.toString();
    }
}
