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
package org.apache.zest.api.association;

import org.apache.zest.api.entity.EntityReference;

/**
 * If you want to catch getting and setting association, then create a GenericConcern
 * that wraps the Zest-supplied Association instance with AssociationWrappers. Override
 * get() and/or set() to perform your custom code.
 */
public class AssociationWrapper
    implements Association<Object>
{
    protected Association<Object> next;

    public AssociationWrapper( Association<Object> next )
    {
        this.next = next;
    }

    public Association<Object> next()
    {
        return next;
    }

    @Override
    public Object get()
    {
        return next.get();
    }

    @Override
    public void set( Object associated )
        throws IllegalArgumentException
    {
        next.set( associated );
    }

    @Override
    public EntityReference reference()
    {
        return next.reference();
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
