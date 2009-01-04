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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The DataFile has been extended at the end.
 *
 * To undo this we simply set the Length of the file to the previously known length.
 * Block Structure
 * [blockSize]     4 bytes
 * [usage]         1 byte    (0=Unused, 1=prime, 2=mirror, 3=primeChanged, 4=mirrorChanged)
 * [instanceVersion] 8 bytes
 * [schemaVersion] 4 bytes
 * [identitySize]  1 byte
 * [identity]      IDENTITY_MAX_LENGTH bytes
 * [mirrorPointer] 8 bytes
 * [primeDataLength] 4 bytes
 * [primeData]     n bytes
 * [mirrorDataLength] 4 bytes
 * [mirrorData]    n bytes
 */
public class UndoExtendCommand
    implements UndoCommand
{
    private long previousLength;

    public UndoExtendCommand( long previousLength )
    {
        this.previousLength = previousLength;
    }

    public void undo( RandomAccessFile dataFile, IdentityFile idFile ) throws IOException
    {
        dataFile.setLength( previousLength );
    }

    public void save( RandomAccessFile undoJournal ) throws IOException
    {
        undoJournal.writeLong( previousLength );
    }

    static UndoExtendCommand load( RandomAccessFile undoJournal )
        throws IOException
    {
        long position = undoJournal.readLong();
        return new UndoExtendCommand( position );
    }
}
