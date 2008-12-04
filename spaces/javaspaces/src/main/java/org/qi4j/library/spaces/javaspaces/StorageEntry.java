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
package org.qi4j.library.spaces.javaspaces;

import net.jini.core.entry.Entry;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public final class StorageEntry
    implements Entry
{
    public String identity;
    public byte[] payload;

    public StorageEntry()
    {
    }

    public StorageEntry( String id, Serializable entry )
    {
        identity = id;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream( out );
            oos.writeObject( entry );
            oos.flush();
            oos.close();
            payload = out.toByteArray();
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
            } catch( IOException e )
            {
                // ignore.
            }
        }
    }

    public StorageEntry( String id )
    {
        identity = id;
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
            Serializable entry = (Serializable) ois.readObject();
            return entry;
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
}
