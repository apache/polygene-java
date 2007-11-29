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
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.entity.EntitySession;


public class SerializedObject
    implements Serializable
{
    private byte[] data;

    public SerializedObject( Object value )
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

    public Object getObject( EntitySession session, CompositeBuilderFactory factory )
        throws ClassNotFoundException
    {
        try
        {
            ByteArrayInputStream in = new ByteArrayInputStream( data );
            CompositeInputStream stream = new CompositeInputStream( in, session, factory );
            return stream.readObject();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
            throw new IllegalStateException( "This exception should not be possible.", e );
        }
    }

}
