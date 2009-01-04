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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.entitystore.swift.IdentityFile;
import org.qi4j.entitystore.swift.IdentityTooLongException;

public class IdentityFileTest
{
    private File idFile;
    private IdentityFile file;

    @Test
    public void whenCreatingNewIdentityFileThenSucceed()
        throws Exception
    {
        idFile = new File( "quick-store" );
        idFile.mkdirs();
        file = IdentityFile.create( idFile, 64, 1000 );
    }

    @Test
    public void whenCreatingAnEntryThenGetTheResultBack()
        throws Exception
    {
        idFile = new File( "quick-store" );
        idFile.mkdirs();
        file = IdentityFile.create( idFile, 64, 1000 );
        long value = 9783249823L;
        QualifiedIdentity identity = createIdentity( "SomeIdentity" );
        file.remember( identity, value );
        long recalled = file.find( identity );
        Assert.assertEquals( "Wrong position retrieved for item.", value, recalled );
    }


    @Test
    public void whenCreating50EntriesThenGetTheResultBack()
        throws Exception
    {
        idFile = new File( "quick-store" );
        idFile.mkdirs();
        file = IdentityFile.create( idFile, 64, 1000 );
        long[] pos = new long[50];
        for( int i = 0; i < 50; i++ )
        {
            pos[ i ] = (long) ( Math.random() * Long.MAX_VALUE );
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            file.remember( identity, pos[ i ] );
        }

        for( int i = 0; i < 50; i++ )
        {
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            long recalled = file.find( identity );
            Assert.assertEquals( "Wrong position retrieved for item " + i + ".", pos[ i ], recalled );
        }
    }

    @Test
    public void whenIdentityIsLongerThanAllowedExpectException()
        throws Exception
    {
        idFile = new File( "quick-store" );
        idFile.mkdirs();
        file = IdentityFile.create( idFile, 24, 1000 );
        try
        {
            QualifiedIdentity identity = createIdentity( "12345678901" );
            file.remember( identity, 827349813743908274L );
            Assert.fail( "Should not allow this long identity." );
        }
        catch( IdentityTooLongException e )
        {
            // expected
        }
    }

    @Test
    public void whenNotEnoughSpaceIsAvailableExpectThatCanStillStore()
        throws Exception
    {
        idFile = new File( "quick-store" );
        idFile.mkdirs();
        file = IdentityFile.create( idFile, 64, 1 );
        long[] pos = new long[150];
        for( int i = 0; i < 150; i++ )
        {
            pos[ i ] = (long) ( Math.random() * Long.MAX_VALUE );
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            file.remember( identity, pos[ i ] );
        }

        for( int i = 0; i < 150; i++ )
        {
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            long recalled = file.find( identity );
            Assert.assertEquals( "Wrong position retrieved for item " + i + ".", pos[ i ], recalled );
        }
    }

    @Test
    public void whenDroppingAnIdentityExpectMinusOneWhenLookingUp()
        throws Exception
    {
        idFile = new File( "quick-store" );
        idFile.mkdirs();
        file = IdentityFile.create( idFile, 64, 5 );
        long[] pos = new long[150];
        for( int i = 0; i < 150; i++ )
        {
            pos[ i ] = (long) ( Math.random() * Long.MAX_VALUE );
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            file.remember( identity, pos[ i ] );
        }

        for( int i = 0; i < 150; i++ )
        {
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            file.drop( identity );
        }

        for( int i = 0; i < 150; i++ )
        {
            QualifiedIdentity identity = createIdentity( "Identity-" + i );
            Assert.assertEquals( "Identity entry not gone.", -1, file.find( identity ) );
        }
    }

    @After
    public void cleanUp()
    {
        try
        {
            file.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        delete( idFile );
    }

    private void delete( File file )
    {
        if( file.isDirectory() )
        {
            for( File child : file.listFiles() )
            {
                delete( child );
            }
        }
        file.delete();
    }

    private QualifiedIdentity createIdentity( String identity )
    {
        return QualifiedIdentity.parseQualifiedIdentity( "org.qi4j.entity.test.Data:" + identity );
    }
}
