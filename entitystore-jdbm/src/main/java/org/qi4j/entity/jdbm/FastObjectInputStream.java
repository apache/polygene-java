package org.qi4j.entity.jdbm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * TODO
 */
public class FastObjectInputStream
    extends ObjectInputStream
{
    public FastObjectInputStream( InputStream inputStream )
        throws IOException
    {
        super( inputStream );
    }

    public FastObjectInputStream()
        throws IOException, SecurityException
    {
    }

    @Override protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException
    {
        String className = readUTF();
        return ObjectStreamClass.lookup( Class.forName( className ) );
    }
}
