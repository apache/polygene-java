/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.spaces.simple;

import java.util.Iterator;

public class ChainedIterator<E, F>
    implements Iterator<F>
{
    private Iterator<E>[] iterators;
    private int pos;
    private Converter<E, F> converter;

    public ChainedIterator( Converter<E, F> converter, Iterator<E>... iterators )
    {
        this.converter = converter;
        if( converter == null )
        {
            this.converter = new NoConverter();
        }
        pos = 0;
        this.iterators = iterators;
    }

    public boolean hasNext()
    {
        for( int i = pos; i < iterators.length; i++ )
        {
            if( iterators[ i ].hasNext() )
            {
                return true;
            }
        }
        return false;
    }

    public F next()
    {
        if( pos < iterators.length )
        {
            if( !iterators[ pos ].hasNext() )
            {
                pos++;
            }
            if( pos == iterators.length )
            {
                return null;
            }
            return converter.convert( iterators[ pos ].next() );
        }
        return null;
    }

    public void remove()
    {
        iterators[ pos ].remove();
    }

    public class NoConverter
        implements Converter
    {

        public Object convert( Object data )
        {
            return data;
        }
    }

    public interface Converter<E, T>
    {
        T convert( E data );
    }
}
