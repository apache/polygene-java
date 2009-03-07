/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.entitystore.coherence;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.api.common.QualifiedName;

class CoherenceEntityState extends DefaultEntityState
    implements PortableObject, EntityState
{
    public CoherenceEntityState( QualifiedIdentity identity, EntityType entityType )
    {
        super( identity, entityType );
    }

    public CoherenceEntityState( long version, long lastModified, QualifiedIdentity identity, EntityStatus status, EntityType entityType, Map<QualifiedName, Object> properties, Map<QualifiedName, QualifiedIdentity> associations, Map<QualifiedName, Collection<QualifiedIdentity>> manyAssociations )
    {
        super( version, lastModified, identity, status, entityType, properties, associations, manyAssociations );
    }

    public void readExternal( PofReader pofReader )
        throws IOException
    {
        int counter = 0;
        version = pofReader.readLong( counter++ );
        lastModified = pofReader.readLong( counter++ );
        ArrayList<String> propertyNames = new ArrayList<String>();
        String propName = pofReader.readString( counter++ );
        while( propName.length() > 0 )
        {
            propertyNames.add( propName );
            propName = pofReader.readString( counter++ );
        }
        int propertyCounter = pofReader.readInt( counter++ );
        for( int i = 0; i < propertyCounter; i++ )
        {
            Object propertyValue = pofReader.readObject( i + counter );
            properties.put(QualifiedName.fromQN(propertyNames.get(i)), propertyValue);
        }
        pofReader.readRemainder();
        clearModified();
    }

    public void writeExternal( PofWriter pofWriter )
        throws IOException
    {
        int counter = 0;
        if( lastModified == 0 )
        {
            lastModified = System.currentTimeMillis();
        }
        pofWriter.writeLong( counter++, version );
        pofWriter.writeLong( counter++, lastModified );
        for( QualifiedName propName : properties.keySet() )
        {
            pofWriter.writeString( counter++, propName.toString() );
        }
        pofWriter.writeString( counter++, "" );
        pofWriter.writeInt( counter, counter - 1 );
        counter++;
        for( QualifiedName propName : properties.keySet() )
        {
            Object propertyValue = getProperty( propName );
            pofWriter.writeObject( counter++, propertyValue );

// IFF the writeObject() above works, then we can toss away the stuff below.
//            if( propertyValue instanceof String )
//            {
//                pofWriter.writeString( counter++, (String) propertyValue );
//            }
//            else if( propertyValue instanceof BigDecimal )
//            {
//                pofWriter.writeBigDecimal( counter++, (BigDecimal) propertyValue );
//            }
//            else if( propertyValue instanceof BigInteger )
//            {
//                pofWriter.writeBigInteger( counter++, (BigInteger) propertyValue );
//
//            }
//            else if( propertyValue instanceof Date )
//            {
//                pofWriter.writeDateTime( counter++, (Date) propertyValue );
//
//            }
//            else if( propertyValue instanceof Long )
//            {
//
//                pofWriter.writeLong( counter++, (Long) propertyValue );
//
//            }
//            else if( propertyValue instanceof long[])
//            {
//                pofWriter.writeLongArray( counter++, (long[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Boolean)
//            {
//                pofWriter.writeBoolean( counter++, (Boolean) propertyValue );
//
//            }
//            else if( propertyValue instanceof boolean[])
//            {
//                pofWriter.writeBooleanArray( counter++, (boolean[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Integer)
//            {
//                pofWriter.writeInt( counter++, (Integer) propertyValue );
//
//            }
//            else if( propertyValue instanceof int[])
//            {
//                pofWriter.writeIntArray( counter++, (int[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Byte )
//            {
//                pofWriter.writeByte( counter++, (Byte) propertyValue );
//
//
//            }
//            else if( propertyValue instanceof byte[])
//            {
//                pofWriter.writeByteArray( counter++, (byte[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Character )
//            {
//                pofWriter.writeChar( counter++, (Character) propertyValue );
//
//            }
//            else if( propertyValue instanceof char[] )
//            {
//
//                pofWriter.writeCharArray( counter++, (char[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Double)
//            {
//
//                pofWriter.writeDouble( counter++, (Double) propertyValue );
//
//            }
//            else if( propertyValue instanceof double[])
//            {
//                pofWriter.writeDoubleArray( counter++, (double[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Float)
//            {
//                pofWriter.writeFloat( counter++, (Float) propertyValue );
//
//            }
//            else if( propertyValue instanceof float[])
//            {
//                pofWriter.writeFloatArray( counter++, (float[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Short)
//            {
//                pofWriter.writeShort( counter++, (Short) propertyValue );
//
//            }
//            else if( propertyValue instanceof short[])
//            {
//                pofWriter.writeShortArray( counter++, (short[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Collection )
//            {
//                pofWriter.writeCollection( counter++, (Collection) propertyValue );
//
//            }
//            else if( propertyValue instanceof Map)
//            {
//                pofWriter.writeMap( counter++, (Map) propertyValue );
//
//            }
//            else if( propertyValue instanceof Object[] )
//            {
//                pofWriter.writeObjectArray( counter++, (Object[]) propertyValue );
//
//            }
//            else if( propertyValue instanceof Serializable )
//            {
//                pofWriter.writeObject( counter++, propertyValue );
//
//            }
        }
    }

    void increaseVersion()
    {
        version++;
    }

    public void markAsLoaded()
    {
        status = EntityStatus.LOADED;
    }
}
