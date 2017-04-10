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
package org.apache.polygene.api.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public final class AccessibleObjects
{
    /**
     * Ensure that a Method, Field, Constructor is accessible.
     *
     * If it is public, do nothing.
     * Otherwise, use {@link AccessibleObject#setAccessible(boolean)} if it hasn't already been done.
     *
     * @param accessibleObject The AccessibleObject
     * @param <T> AccessibleObject type
     * @return The given AccessibleObject, accessible
     */
    public static <T extends AccessibleObject> T accessible( T accessibleObject )
    {
        if( accessibleObject instanceof Member )
        {
            Member member = (Member) accessibleObject;
            if( Modifier.isPublic( member.getModifiers() )
                && Modifier.isPublic( member.getDeclaringClass().getModifiers() ) )
            {
                return accessibleObject;
            }
        }
        if( !accessibleObject.isAccessible() )
        {
            accessibleObject.setAccessible( true );
        }
        return accessibleObject;
    }

    private AccessibleObjects() {}
}
