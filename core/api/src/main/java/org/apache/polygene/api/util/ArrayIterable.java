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

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Iterate over arrays, both primitive arrays and Object[].
 */
public class ArrayIterable implements Iterable<Object>
{
    private final Object array;

    public ArrayIterable( final Object array )
    {
        if( !array.getClass().isArray() )
        {
            throw new IllegalArgumentException( array + " is not an array" );
        }
        this.array = array;
    }

    @Override
    public Iterator<Object> iterator()
    {
        return new ArrayIterator( array );
    }

    private class ArrayIterator implements Iterator<Object>
    {
        private final Object array;
        private int currentIndex = 0;

        private ArrayIterator( Object array )
        {
            this.array = array;
        }

        @Override
        public boolean hasNext()
        {
            return currentIndex < Array.getLength( array );
        }

        @Override
        public Object next()
        {
            return Array.get( array, currentIndex++ );
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException( "cannot remove items from an array" );
        }
    }
}
