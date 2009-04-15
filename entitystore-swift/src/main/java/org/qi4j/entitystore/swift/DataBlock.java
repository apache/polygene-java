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
package org.qi4j.entitystore.swift;

import java.util.Arrays;
import org.qi4j.api.entity.EntityReference;

class DataBlock
{
    EntityReference reference;
    byte[] data;
    long instanceVersion;
    int schemaVersion;

    public DataBlock( EntityReference reference, byte[] data, long instanceVersion, int schemaVersion )
    {
        this.reference = reference;
        this.data = data;
        this.instanceVersion = instanceVersion;
        this.schemaVersion = schemaVersion;
    }

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

        DataBlock dataBlock = (DataBlock) o;

        if( instanceVersion != dataBlock.instanceVersion )
        {
            return false;
        }
        if( schemaVersion != dataBlock.schemaVersion )
        {
            return false;
        }
        if( !Arrays.equals( data, dataBlock.data ) )
        {
            return false;
        }
        if( !reference.equals( dataBlock.reference) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = reference.hashCode();
        result = 31 * result + Arrays.hashCode( data );
        result = 31 * result + (int) ( instanceVersion ^ ( instanceVersion >>> 32 ) );
        result = 31 * result + schemaVersion;
        return result;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( schemaVersion );
        buf.append( '[' );
        buf.append(reference);
        buf.append( ':' );
        buf.append( instanceVersion );
        buf.append( '{' );
        boolean first = true;
        for( byte b : data )
        {
            if (!first)
            {
                buf.append( ',' );
            }
            first = false;
            buf.append( toHex( b ) );
        }
        buf.append( '}' );
        buf.append( ']' );
        return buf.toString();
    }

    private String toHex( byte b )
    {
        int data = b;

        if(data < 0 )
        data = data +256;
        if( data < 16 )
        {
            return "0" + Integer.toHexString( data );
        }
        return Integer.toHexString( data );
    }
}
