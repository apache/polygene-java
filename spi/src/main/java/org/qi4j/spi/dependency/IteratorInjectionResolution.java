/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.spi.dependency;

import java.util.Iterator;

/**
 * If a dependency resolution refers to an iterator, use this
 * to return a new instance each time.
 * <p/>
 * This can be useful to implement the Prototype design pattern
 * in combination with CompositeBuilder's or ObjectBuilder's.
 */
public class IteratorInjectionResolution
    implements InjectionProvider
{
    private Iterator prototypeIterator;

    public IteratorInjectionResolution( Iterable prototypeIterable )
    {
        this( prototypeIterable.iterator() );
    }

    public IteratorInjectionResolution( Iterator prototypeIterator )
    {
        this.prototypeIterator = prototypeIterator;
    }

    public Object provideInjection( InjectionContext context )
    {
        if( prototypeIterator.hasNext() )
        {
            return prototypeIterator.next();
        }
        else
        {
            return null;
        }
    }
}
