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

import org.qi4j.spi.entity.QualifiedIdentity;
import java.util.Iterator;
import java.io.RandomAccessFile;
import java.io.IOException;

class StoreIterator
    implements Iterator<QualifiedIdentity>
{
    private boolean isAvailable;
    private RandomAccessFile store;
    private QualifiedIdentity identity;
    private long position;
    private int identityMaxLength;

    StoreIterator( RandomAccessFile store, int identityMaxLength )
    {
        this.store = store;
        this.position = DataStore.DATA_AREA_OFFSET;
        this.identityMaxLength = identityMaxLength;
        getNext();
    }

    public boolean hasNext()
    {
        return isAvailable;
    }

    public QualifiedIdentity next()
    {
        QualifiedIdentity result = identity;
        getNext();
        return result;
    }

    private void getNext()
    {
        try
        {
            while( store.getFilePointer() < store.length() )
            {
                store.seek( position );
                int blockSize = store.readInt();
                if (blockSize == 0)
                {
                    // TODO This is a bug. Why does it occur??
                    isAvailable = false;
                    return;
                }
                position = position + blockSize;  // position for next round...
                byte usage = store.readByte();
                if( usage == 1 || usage == 2 )
                {
                    store.skipBytes( 12 );
                    identity = readIdentity();
                    isAvailable = true;
                    return;
                }
            }
            isAvailable = false;
        }
        catch( IOException e )
        {
            identity = null;
            isAvailable = false;
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private QualifiedIdentity readIdentity()
        throws IOException
    {
        int idSize = store.readByte();
        if( idSize < 0 )
        {
            idSize = idSize + 256;  // Fix 2's-complement negative values of bytes into unsigned 8 bit.
        }
        byte[] idData = new byte[idSize];
        store.read( idData );
        store.skipBytes( identityMaxLength - idSize );
        return QualifiedIdentity.parseQualifiedIdentity( new String( idData ) );
    }
}
