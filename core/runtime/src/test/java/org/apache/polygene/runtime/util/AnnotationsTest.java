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
package org.apache.polygene.runtime.util;

import java.lang.reflect.Type;
import java.util.Collection;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.sideeffect.SideEffects;
import org.apache.polygene.api.util.Annotations;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

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
        assertThat( "Mixins annotation found", Annotations.annotationOn( AnnotatedClass.class, Mixins.class ), notNullValue() );

        assertThat( "No SideEffects annotation found", Annotations.annotationOn( AnnotatedClass.class, SideEffects.class ), nullValue() );

        final Type returnType = AnnotatedClass.class.getDeclaredMethod( "list" ).getGenericReturnType();
        assertThat( "Null on no class type", Annotations.annotationOn( returnType, Mixins.class ), nullValue() );
    }
}
