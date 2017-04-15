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
package org.apache.polygene.library.sql.generator.vendor;

import org.apache.polygene.library.sql.generator.grammar.common.SQLStatement;

/**
 * This exception will typically be thrown by
 * {@link SQLVendor#toString(SQLStatement)} method when the vendor encounters a
 * SQL syntax element that the vendor doesn't understand.
 *
 * @author Stanislav Muhametsin
 */
public class UnsupportedElementException extends RuntimeException
{

    private static final long serialVersionUID = -5331011803322815958L;

    private final Object _element;

    public UnsupportedElementException( String msg )
    {
        this( msg, null );
    }

    public UnsupportedElementException( String msg, Object element )
    {
        super( msg );
        this._element = element;
    }

    public Object getElement()
    {
        return this._element;
    }
}
