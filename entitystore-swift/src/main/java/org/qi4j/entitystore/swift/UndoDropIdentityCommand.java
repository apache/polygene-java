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

import java.io.RandomAccessFile;
import java.io.IOException;
import org.qi4j.spi.entity.QualifiedIdentity;

public class UndoDropIdentityCommand
    implements UndoCommand
{
    private QualifiedIdentity identity;
    private long position;

    public UndoDropIdentityCommand( QualifiedIdentity identity, long position )
    {
        this.identity = identity;
        this.position = position;
    }

    public void undo( RandomAccessFile dataFile, IdentityFile idFile ) throws IOException
    {
        idFile.remember( identity, position );
    }

    public void save( RandomAccessFile undoJournal ) throws IOException
    {
        undoJournal.writeUTF( identity.toString() );
        undoJournal.writeLong( position );
    }

    static UndoDropIdentityCommand load( RandomAccessFile undoJournal )
        throws IOException
    {
        String idString = undoJournal.readUTF();
        QualifiedIdentity identity = QualifiedIdentity.parseQualifiedIdentity( idString );
        long pos = undoJournal.readLong();
        return new UndoDropIdentityCommand( identity, pos );
    }
}
