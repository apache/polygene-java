/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.http;

import java.io.Serializable;

public class ConstraintInfo
        implements Serializable
{

    public static enum Constraint
    {
    }

    public static enum HttpMethod
    {

        GET, POST, HEAD, PUT, OPTIONS, TRACE, DELETE;

        public static String[] toStringArray( HttpMethod[] methods )
        {
            String[] result = new String[ methods.length ];
            for ( int idx = 0; idx < methods.length; idx++ ) {
                result[idx] = methods[idx].name();
            }
            return result;
        }

    }

    private final String path;

    private final Constraint constraint;

    private final HttpMethod[] omittedHttpMethods;

    ConstraintInfo( String path, Constraint constraint, HttpMethod[] omittedHttpMethods )
    {
        this.path = path;
        this.constraint = constraint;
        this.omittedHttpMethods = omittedHttpMethods;
    }

    public Constraint getConstraint()
    {
        return constraint;
    }

    public HttpMethod[] getOmittedHttpMethods()
    {
        return omittedHttpMethods;
    }

    public String getPath()
    {
        return path;
    }

}
