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
package org.qi4j.runtime.composite;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.qi4j.bootstrap.BindingException;

/**
 * This class is NOT thread-safe.
 * //TODO: Algorithm need to be optimized.
 */
public final class UsageGraph<K>
{
    private final Collection<K> data;
    private final Use<K> use;
    private final boolean allowCyclic;
    private List<K> resolved;
    private HashMap<K, List<K>> transitive;

    public UsageGraph( Collection<K> data, Use<K> use, boolean allowCyclic )
    {
        this.data = data;
        this.use = use;
        this.allowCyclic = allowCyclic;
    }

    public boolean transitiveUse( K source, K other )
        throws BindingException
    {
        if( transitive == null )
        {
            buildUsageGraph();
        }
        return transitive.containsKey( source ) && transitive.get( source ).contains( other );
    }

    private void checkCyclic( List<K> visited, K sourceItem, K used )
        throws BindingException
    {
        Collection<K> nextLevel = use.uses( used );
        for( K next : nextLevel )
        {
            if( next == sourceItem )
            {
                if( !allowCyclic )
                {
                    visited.add( next );
                    throw new BindingException( "Cyclic usage detected: " + sourceItem + " -> " + visited );
                }
            }
            if( !visited.contains( next ) )
            {
                visited.add( next );
                checkCyclic( visited, sourceItem, next );
            }
        }
    }

    /**
     * Must be called if the data set has been modified.
     */
    public void invalidate()
    {
        resolved = null;
        transitive = null;
    }

    public List<K> resolveOrder()
        throws BindingException
    {
        if( resolved == null )
        {
            buildUsageGraph();
            resolved = new LinkedList<K>();
            for( K item : data )
            {
                int pos = resolved.size();
                for( K entry : resolved )
                {
                    if( transitiveUse( entry, item ) )
                    {
                        pos = resolved.indexOf( entry );
                        break;
                    }
                }
                resolved.add( pos, item );
            }
        }
        return resolved;
    }

    private void buildUsageGraph()
        throws BindingException
    {
        transitive = new HashMap<K, List<K>>();
        for( K sourceItem : data )
        {
            LinkedList<K> visited = new LinkedList<K>();
            checkCyclic( visited, sourceItem, sourceItem );
            transitive.put( sourceItem, visited );
        }
    }

    public interface Use<K>
    {

        /**
         * @param source The item to be queried.
         *
         * @return A list of items it uses.
         */
        Collection<K> uses( K source );
    }
}
