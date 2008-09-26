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
package org.qi4j.library.swing.visualizer.detailPanel.internal.common;

import java.util.LinkedList;
import java.util.List;

/**
 * @author edward.yakop@gmail.com
 */
public final class CollectionUtils
{
    private CollectionUtils()
    {

    }

    /**
     * Create a list given the specified {@code iterable}. Returns an empty list if iterable is {@code null}..
     *
     * @param iterable Iterable to convert.
     * @return A new list given the specified iterable.
     * @since 0.5
     */
    public static <T> List<T> toList( Iterable<T> iterable )
    {
        LinkedList<T> list = new LinkedList<T>();

        if( iterable != null )
        {
            for( T item : iterable )
            {
                list.add( item );
            }
        }

        return list;
    }
}
