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
package org.apache.polygene.api.common;

/**
 * Thrown when a Fragment or object could not be instantiated.
 * This includes, but not be limited to;
 * <ul>
 * <li>private constructor.</li>
 * <li>abstract class for Constraints.</li>
 * <li>interface instead of a class.</li>
 * <li>useful constructor missing.</li>
 * <li>exception thrown in the constructor.</li>
 * <li>Subclassing of org.apache.polygene.api.property.Property</li>
 * </ul>
 * <p>
 * See the nested exception for additional details.
 * </p>
 */
public class ConstructionException
    extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public ConstructionException()
    {
    }

    public ConstructionException( String message )
    {
        super( message );
    }

    public ConstructionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ConstructionException( Throwable cause )
    {
        super( cause );
    }
}
