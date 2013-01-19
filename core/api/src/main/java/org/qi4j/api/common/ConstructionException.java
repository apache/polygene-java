/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.common;

/**
 * Thrown when a Fragment or object could not be instantiated.
 * This includes, but not be limited to;
 * <ul>
 * <li>private constructor.</li>
 * <li>abstract class for Constraints.</li>
 * <li>interface instead of a class.</li>
 * <li>useful constructor missing.</li>
 * <li>exception thrown in the constructor.</li>
 * <li>Subclassing of org.qi4j.api.property.Property</li>
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
