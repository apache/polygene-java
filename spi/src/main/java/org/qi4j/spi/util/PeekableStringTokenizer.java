/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.util;

import java.util.StringTokenizer;

/**
 * JAVADOC
 */
public class PeekableStringTokenizer
    extends StringTokenizer
{
    private String bufferedToken;

    public PeekableStringTokenizer( String str, String delim, boolean returnDelims )
    {
        super( str, delim, returnDelims );
    }

    public PeekableStringTokenizer( String str, String delim )
    {
        super( str, delim );
    }

    public PeekableStringTokenizer( String str )
    {
        super( str );
    }


    public boolean hasMoreTokens()
    {
        if( bufferedToken != null )
        {
            return true;
        }
        else
        {
            return super.hasMoreTokens();
        }
    }

    public String nextToken()
    {
        if( bufferedToken != null )
        {
            String temp = bufferedToken;
            bufferedToken = null;
            return temp;
        }
        else
        {
            return super.nextToken();
        }
    }

    public String nextToken( String delim )
    {
        if( bufferedToken != null )
        {
            String temp = bufferedToken;
            bufferedToken = null;
            return temp;
        }
        else
        {
            return super.nextToken( delim );
        }
    }

    public boolean hasMoreElements()
    {
        if( bufferedToken != null )
        {
            return true;
        }
        else
        {
            return super.hasMoreElements();
        }
    }

    public Object nextElement()
    {
        if( bufferedToken != null )
        {
            String temp = bufferedToken;
            bufferedToken = null;
            return temp;
        }
        else
        {
            return super.nextElement();
        }
    }

    public int countTokens()
    {
        if( bufferedToken != null )
        {
            return super.countTokens() + 1;
        }
        else
        {
            return super.countTokens();
        }
    }

    public String peekNextToken()
    {
        if( bufferedToken != null )
        {
            return bufferedToken;
        }
        else
        {
            bufferedToken = nextToken();
            return bufferedToken;
        }
    }

    public String peekNextToken( String delim )
    {
        if( bufferedToken != null )
        {
            return bufferedToken;
        }
        else
        {
            bufferedToken = nextToken( delim );
            return bufferedToken;
        }
    }
}


