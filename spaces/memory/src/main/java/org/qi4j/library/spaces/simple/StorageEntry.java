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
package org.qi4j.library.spaces.simple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class StorageEntry
{

    public String identity;

    public long version;

    public byte[] payload;

    private StorageState state;

    public StorageEntry( String id, byte[] payload, long version )
    {
        identity = id;
        this.payload = payload;
        this.version = version;
    }

    public StorageEntry( String id, Serializable entry )
    {
        this( id, createPayload( entry ), 0 );
    }

    public String identity()
    {
        return identity;
    }

    public Serializable data()
    {
        ByteArrayInputStream in = new ByteArrayInputStream( payload );
        try
        {
            ObjectInputStream ois = new ObjectInputStream( in );
            return (Serializable) ois.readObject();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        catch( ClassNotFoundException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                in.close();
            }
            catch( IOException e )
            {
                // can not happen.
            }
        }
        return null;
    }

    boolean isUpdated()
    {
        return state == StorageState.updated;
    }

    void setUpdated()
    {
        state = StorageState.updated;
    }

    boolean isLoaded()
    {
        return state == StorageState.loaded;
    }

    void setLoaded()
    {
        state = StorageState.loaded;
    }

    boolean isRemoved()
    {
        return state == StorageState.removed;
    }

    void setRemoved()
    {
        state = StorageState.removed;
    }

    public void setNoState()
    {
        state = StorageState.none;
    }

    private static byte[] createPayload( Serializable entry )
    {
        byte[] result = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream( out );
            oos.writeObject( entry );
            oos.flush();
            oos.close();
            result = out.toByteArray();
        }
        catch( IOException e )
        {
            e.printStackTrace();
            // Ignore, can not happen.
        }
        finally
        {
            try
            {
                out.close();
            }
            catch( IOException e )
            {
                // ignore.
            }
        }
        return result;
    }
}
