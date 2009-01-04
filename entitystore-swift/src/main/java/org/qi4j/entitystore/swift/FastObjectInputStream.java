package org.qi4j.entitystore.swift;

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
    private final boolean usesReadObjectMethod;

    public FastObjectInputStream( InputStream inputStream, boolean usesReadObjectMethod )
        throws IOException
    {
        super( inputStream );
        this.usesReadObjectMethod = usesReadObjectMethod;
    }

    @Override protected ObjectStreamClass readClassDescriptor()
        throws IOException, ClassNotFoundException
    {
        if( usesReadObjectMethod )
        {
            return super.readClassDescriptor();
        }
        else
        {
            String className = readUTF();
            return ObjectStreamClass.lookup( Class.forName( className ) );
        }
    }
}