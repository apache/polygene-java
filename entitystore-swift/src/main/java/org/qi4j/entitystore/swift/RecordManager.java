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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;

public class RecordManager
    implements UndoManager
{
    private static final byte UNDO_DELETE = 2;
    private static final byte UNDO_MODIFY = 3;
    private static final byte UNDO_NEW_IDENTITY = 4;
    private static final byte UNDO_DROP_IDENTITY = 5;
    private static final byte UNDO_EXTEND = 6;

    private DataStore dataStore;
    private RandomAccessFile undoJournal;
    private ArrayList<UndoCommand> commands;

    public RecordManager( File dataDir, boolean recover )
        throws IOException
    {
        File undoFile = new File( dataDir, "undo.data" );
        dataStore = new DataStore( dataDir, this );
        commands = new ArrayList<UndoCommand>();
        if( undoFile.exists() )
        {
            undoJournal = new RandomAccessFile( undoFile, "rw" );
            if( recover && undoJournal.length() > 0 )
            {
                recover();
            }
        }
        else
        {
            undoFile.createNewFile();
            undoJournal = new RandomAccessFile( undoFile, "rw" );
        }

    }

    public void putData( DataBlock data )
        throws IOException
    {
        dataStore.putData( data );
    }

    public void deleteData( QualifiedIdentity identity )
        throws IOException
    {
        dataStore.delete( identity );
    }

    public DataBlock readData( QualifiedIdentity identity )
        throws IOException
    {
        return dataStore.readData( identity );
    }

    public Iterator<QualifiedIdentity> iterator()
    {
        return dataStore.iterator();
    }

    public void commit()
        throws IOException
    {
        commands.clear();
        undoJournal.setLength( 0 );
    }

    public void discard()
        throws IOException
    {
        for( UndoCommand command : commands )
        {
            command.undo( dataStore.dataFile(), dataStore.identityFile() );
        }
        commands.clear();
        undoJournal.setLength( 0 );
    }

    public void close()
        throws IOException
    {
        dataStore.close();
    }

    public void saveUndoCommand( UndoCommand command )
    {
        commands.add( command );
            try
            {
                if( command instanceof UndoDeleteCommand )
                {
                    undoJournal.write( UNDO_DELETE );
                    command.save( undoJournal );
                }
                else if( command instanceof UndoModifyCommand )
                {
                    undoJournal.write( UNDO_MODIFY );
                    command.save( undoJournal );
                }
                else if( command instanceof UndoDropIdentityCommand )
                {
                    undoJournal.write( UNDO_DROP_IDENTITY );
                    command.save( undoJournal );
                }
                else if( command instanceof UndoNewIdentityCommand )
                {
                    undoJournal.write( UNDO_NEW_IDENTITY );
                    command.save( undoJournal );
                }
                else if( command instanceof UndoExtendCommand )
                {
                    undoJournal.write( UNDO_EXTEND );
                    command.save( undoJournal );
                }
                else
                {
                    throw new InternalError();
                }
            }
            catch( IOException e )
            {
                throw new EntityStoreException( "Undo storage medium is malfunctioning." );
            }
    }

    private void recover()
    {
        try
        {
            undoJournal.seek( 0 );
            while( undoJournal.getFilePointer() < undoJournal.length() )
            {
                byte type = undoJournal.readByte();
                UndoCommand command;
                if( type == UNDO_MODIFY )
                {
                    command = UndoModifyCommand.load( undoJournal );
                }
                else if( type == UNDO_DELETE )
                {
                    command = UndoDeleteCommand.load( undoJournal );
                }
                else if( type == UNDO_DROP_IDENTITY )
                {
                    command = UndoDropIdentityCommand.load( undoJournal );
                }
                else if( type == UNDO_EXTEND )
                {
                    command = UndoExtendCommand.load( undoJournal );
                }
                else if( type == UNDO_NEW_IDENTITY )
                {
                    command = UndoNewIdentityCommand.load( undoJournal );
                }
                else
                {
                    throw new InternalError();
                }
                commands.add( command );
            }
            discard();
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Unable to recover from previous crash." );
        }
    }
}
