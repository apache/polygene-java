/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.spi.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import org.qi4j.api.Qi4j;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.composite.CompositeBuilderFactory;


public final class SerializedObject<T>
    implements Serializable
{
    private byte[] data;

    public SerializedObject( byte[] data )
    {
        this.data = data;
    }

    public SerializedObject( T value )
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompositeOutputStream stream = new CompositeOutputStream( out );
            stream.writeObject( value );
            stream.flush();
            data = out.toByteArray();
            stream.close();
            out.close();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
            throw new IllegalStateException( "This exception should not be possible.", e );
        }

    }

    public T getObject( UnitOfWork unitOfWork, Qi4j api )
        throws ClassNotFoundException
    {
        try
        {
            ByteArrayInputStream in = new ByteArrayInputStream( data );
            CompositeInputStream stream = new CompositeInputStream( in, unitOfWork, api );
            return (T) stream.readObject();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
            throw new IllegalStateException( "This exception should not be possible.", e );
        }
    }

    public T getObject( CompositeBuilderFactory cbf, Qi4j api )
        throws ClassNotFoundException
    {
        try
        {
            ByteArrayInputStream in = new ByteArrayInputStream( data );
            CompositeInputStream stream = new CompositeInputStream( in, cbf, api );
            return (T) stream.readObject();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
            throw new IllegalStateException( "This exception should not be possible.", e );
        }
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData( byte[] data )
    {
        this.data = data;
    }
}
