/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.extension.persistence.jdbm;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import jdbm.RecordManagerOptions;
import junit.framework.TestCase;
import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TransactionManager;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.runtime.CompositeBuilderFactoryImpl;
import org.qi4j.runtime.CompositeModelFactoryImpl;

public class JdbmPersistenceTest extends TestCase
{
    private JdbmStorage underTest;
    private File testDir;

    public void testGetProperties()
        throws Exception
    {
        Method method = JdbmStorage.class.getDeclaredMethod( "getProperties", new Class[]{ File.class } );
        method.setAccessible( true );
        Properties p = (Properties) method.invoke( underTest, testDir );
        assertEquals( "false", p.getProperty( RecordManagerOptions.AUTO_COMMIT) );
        assertEquals( "1000", p.getProperty( RecordManagerOptions.CACHE_SIZE ));
        assertEquals( "false", p.getProperty( RecordManagerOptions.DISABLE_TRANSACTIONS) );
        assertEquals( RecordManagerOptions.NORMAL_CACHE, p.getProperty( RecordManagerOptions.CACHE_TYPE ) );
        assertEquals( "false", p.getProperty( RecordManagerOptions.THREAD_SAFE ) );

    }

    protected void setUp() throws Exception
    {
        File userHome = new File( System.getProperty( "user.home" ) );
        testDir = new File( userHome, ".junit-qi4j" );
        testDir.mkdirs();
        testDir.deleteOnExit();
        File propsFile = new File( testDir, "qi4j.properties" );
        propsFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream( propsFile );
        Properties p = new Properties();
        p.put( RecordManagerOptions.AUTO_COMMIT, "false" );
        p.put( RecordManagerOptions.CACHE_SIZE, "1000" );
        p.put( RecordManagerOptions.DISABLE_TRANSACTIONS, "false" );
        p.put( RecordManagerOptions.CACHE_TYPE, RecordManagerOptions.NORMAL_CACHE );
        p.put( RecordManagerOptions.THREAD_SAFE, "false" );
        p.store( fos, "" );
        fos.close();
        CompositeBuilderFactory builderFactory = new CompositeBuilderFactoryImpl();
        Jotm jotm = new Jotm( true, false );
        TransactionManager transactionManager = jotm.getTransactionManager();
        underTest = new JdbmStorage( builderFactory, testDir, transactionManager );
    }
}
