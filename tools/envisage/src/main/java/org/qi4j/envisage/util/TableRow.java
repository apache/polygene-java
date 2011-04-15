/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.envisage.util;

import java.util.ArrayList;

/**
 * A simple data structure like table row
 */
public class TableRow
{

    protected ArrayList data;

    public TableRow( int col )
    {
        this( col, null );
    }

    public TableRow( int col, Object[] values )
    {
        data = new ArrayList( col );

        for( int i = 0; i < col; i++ )
        {
            if( values == null )
            {
                set( i, null );
            }
            else
            {
                if( values.length > i )
                {
                    set( i, values[ i ] );
                }
                else
                {
                    set( i, values[ i ] );
                }
            }
        }
    }

    public void set( int col, Object object )
    {
        data.add( col, object );
    }

    public Object get( int col )
    {
        return data.get( col );
    }
}
