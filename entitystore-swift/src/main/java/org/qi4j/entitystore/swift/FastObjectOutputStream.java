package org.qi4j.entitystore.swift;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * TODO
 */
public class FastObjectOutputStream
    extends ObjectOutputStream
{
    private final boolean usesWriteObjectMethod;

    public FastObjectOutputStream( OutputStream outputStream, boolean usesWriteObjectMethod )
        throws IOException
    {
        super( outputStream );
        this.usesWriteObjectMethod = usesWriteObjectMethod;
    }

    @Override protected void writeClassDescriptor( ObjectStreamClass objectStreamClass )
        throws IOException
    {
        if( usesWriteObjectMethod )
        {
            super.writeClassDescriptor( objectStreamClass );
        }
        else
        {
            writeUTF( objectStreamClass.getName() );
        }
    }
}