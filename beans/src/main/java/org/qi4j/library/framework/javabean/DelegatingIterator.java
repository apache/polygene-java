/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.library.framework.javabean;

import java.util.Iterator;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.composite.CompositeBuilderFactory;

public class DelegatingIterator
    implements Iterator
{
    private Iterator source;
    private final AssociationInfo info;
    private final CompositeBuilderFactory cbf;

    public DelegatingIterator( Iterator source, AssociationInfo info, CompositeBuilderFactory cbf )
    {
        this.source = source;
        this.info = info;
        this.cbf = cbf;
    }

    public boolean hasNext()
    {
        return source.hasNext();
    }

    public Object next()
    {
        return Wrapper.wrap( source.next(), info, cbf );
    }

    public void remove()
    {
        source.remove();
    }
}
