package org.qi4j.entity.jdbm;

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
    public FastObjectOutputStream( OutputStream outputStream ) throws IOException
    {
        super( outputStream );
    }

    protected FastObjectOutputStream() throws IOException, SecurityException
    {
        super();
    }

    @Override protected void writeClassDescriptor( ObjectStreamClass objectStreamClass ) throws IOException
    {
        writeUTF( objectStreamClass.getName() );
    }
}
