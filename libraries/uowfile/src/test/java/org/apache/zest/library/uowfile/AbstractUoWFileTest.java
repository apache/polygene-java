/*
 * Copyright 2011-2014 Paul Merlin.
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
package org.apache.zest.library.uowfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Outputs;
import org.apache.zest.test.AbstractZestTest;

public abstract class AbstractUoWFileTest
    extends AbstractZestTest
{
    protected static File baseTestDir;

    @BeforeClass
    public static void beforeClass()
        throws IOException
    {
        File testDir = new File( "build/uowfiletest" );
        if( !testDir.exists() )
        {
            if( !testDir.mkdirs() )
            {
                throw new IOException( "Unable to create directory: " + testDir );
            }
        }
        baseTestDir = testDir;
    }

    @AfterClass
    public static void afterClass()
    {
        // Delete test data
        Stack<File> stack = new Stack<>();
        stack.push( baseTestDir );
        while( !stack.empty() )
        {
            File each = stack.peek();
            if( each.isDirectory() )
            {
                File[] children = each.listFiles();
                if( children.length > 0 )
                {
                    for( File child : children )
                    {
                        stack.push( child );
                    }
                }
                else
                {
                    stack.pop().delete();
                }
            }
            else
            {
                stack.pop().delete();
            }
        }
    }

    protected final boolean isFileFirstLineEqualsTo( File file, String start )
        throws IOException
    {
        List<String> lines = new ArrayList<>();
        // This load the full file but used test resources are single line files
        Inputs.text( file ).transferTo( Outputs.collection( lines ) );
        return lines.get( 0 ).trim().startsWith( start );
    }
}
