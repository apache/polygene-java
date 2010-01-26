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

package org.qi4j.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Map whose values are Lists of things. Create
 * one ArrayList for each key that is added. The list does not allow
 * duplicates.
 */
public final class ListMap<K, V>
    extends HashMap<K, List<V>>
{
    public void add( K key, V value )
    {
        List<V> list = get( key );
        if( list == null )
        {
            list = new ArrayList<V>();
            put( key, list );
        }
        if( !list.contains( value ) )
        {
            list.add( value );
        }
    }
}