/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.mixin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Generic mixin that is a no-op. Can be useful if the functionality
 * of a method is mainly provided by concerns and side-effects.
 */
public final class NoopMixin
    implements InvocationHandler
{
    private static final Boolean BOOLEAN_DEFAULT = Boolean.FALSE;
    private static final Short SHORT_DEFAULT = 0;
    private static final Character CHARACTER_DEFAULT = 0;
    private static final Integer INTEGER_DEFAULT = 0;
    private static final Long LONG_DEFAULT = 0L;
    private static final Float FLOAT_DEFAULT = 0f;
    private static final Double DOUBLE_DEFAULT = 0.0;

    @Override
    public Object invoke( Object object, Method method, Object[] args )
        throws Throwable
    {
        Class<?> retType = method.getReturnType();
        if( !retType.isPrimitive() )
        {
            return null;
        }
        if( Void.TYPE == retType )
        {
            return null;
        }
        if( Boolean.TYPE == retType )
        {
            return BOOLEAN_DEFAULT;
        }
        if( Short.TYPE == retType )
        {
            return SHORT_DEFAULT;
        }
        if( Character.TYPE == retType )
        {
            return CHARACTER_DEFAULT;
        }
        if( Integer.TYPE == retType )
        {
            return INTEGER_DEFAULT;
        }
        if( Long.TYPE == retType )
        {
            return LONG_DEFAULT;
        }
        if( Float.TYPE == retType )
        {
            return FLOAT_DEFAULT;
        }
        if( Double.TYPE == retType )
        {
            return DOUBLE_DEFAULT;
        }
        return null;
    }
}
