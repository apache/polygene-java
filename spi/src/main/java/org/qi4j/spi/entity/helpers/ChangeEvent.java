/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity.helpers;

import java.io.Serializable;

/**
 * JAVADOC
 */
public class ChangeEvent
    implements Serializable
{
    private String identity;
    private long timeStamp;

    public ChangeEvent( String identity, long timeStamp )
    {
        this.identity = identity;
        this.timeStamp = timeStamp;
    }

    public String identity()
    {
        return identity;
    }

    public long timeStamp()
    {
        return timeStamp;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ChangeEvent that = (ChangeEvent) o;

        if( !identity.equals( that.identity ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return identity.hashCode();
    }
}
