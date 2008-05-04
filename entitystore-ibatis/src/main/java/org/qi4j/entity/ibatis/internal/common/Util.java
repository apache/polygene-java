/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis.internal.common;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class Util
{
    private Util()
    {
    }

    /**
     * Returns {@code true} if both arguments are equals.
     *
     * @param aValue       A value.
     * @param anotherValue Another value.
     * @return A {@code boolean} indicator whether both arguments are equal.
     * @since 0.1.0
     */
    public static boolean isNotEquals( Object aValue, Object anotherValue )
    {
        if( aValue == anotherValue )
        {
            return false;
        }

        return ( aValue != null ) ? !aValue.equals( anotherValue ) : !anotherValue.equals( aValue );
    }
}
