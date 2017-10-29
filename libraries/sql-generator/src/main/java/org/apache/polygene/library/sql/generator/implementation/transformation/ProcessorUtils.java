/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.sql.generator.implementation.transformation;

import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.common.SetQuantifier;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropBehaviour;

/**
 *
 */
public class ProcessorUtils
{
    public static Boolean notNullAndNotEmpty( String str )
    {
        return str != null && str.trim().length() > 0;
    }

    public static void processSetQuantifier( SetQuantifier quantifier, StringBuilder builder )
    {
        if( quantifier == SetQuantifier.ALL )
        {
            builder.append( "ALL" );
        }
        else
        {
            builder.append( "DISTINCT" );
        }
    }

    public static void processDropBehaviour( DropBehaviour behaviour, StringBuilder builder )
    {
        builder.append( SQLConstants.TOKEN_SEPARATOR );
        if( behaviour == DropBehaviour.CASCADE )
        {
            builder.append( "CASCADE" );
        }
        else
        {
            builder.append( "RESTRICT" );
        }
    }
}
