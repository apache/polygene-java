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
package org.apache.zest.runtime.util;

import java.lang.reflect.Type;
import java.util.Collection;
import org.junit.Test;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.api.util.Annotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AnnotationsTest
{
    @Mixins( value = AnnotatedClass.class )
    interface AnnotatedClass<T>
    {
        Collection<T> list();
    }

    @Test
    public void getAnnotationOrNull()
        throws NoSuchMethodException
    {
        assertNotNull( "Mixins annotation found", Annotations.annotationOn( AnnotatedClass.class, Mixins.class ) );

        assertNull( "No SideEffects annotation found", Annotations.annotationOn( AnnotatedClass.class, SideEffects.class ) );

        final Type returnType = AnnotatedClass.class.getDeclaredMethod( "list" ).getGenericReturnType();
        assertNull( "Null on no class type", Annotations.annotationOn( returnType, Mixins.class ) );
    }
}
