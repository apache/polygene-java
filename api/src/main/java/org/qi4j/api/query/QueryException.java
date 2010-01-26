/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.query;

/**
 * Base class for Query exceptions.
 */
public abstract class QueryException
    extends RuntimeException
{
    private static final long serialVersionUID = -3602596752342902060L;

    public QueryException()
    {
    }

    public QueryException( final String message )
    {
        super( message );
    }

    public QueryException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
