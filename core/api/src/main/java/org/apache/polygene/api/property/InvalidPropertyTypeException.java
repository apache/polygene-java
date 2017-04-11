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
package org.apache.polygene.api.property;

import java.lang.reflect.AccessibleObject;
import org.apache.polygene.api.common.ConstructionException;

/**
 * Thrown when attempting to subclass Property.
 */
public class InvalidPropertyTypeException extends ConstructionException
{
    public InvalidPropertyTypeException( String message )
    {
        super(message);
    }

    public InvalidPropertyTypeException( AccessibleObject accessor )
    {
        super( createMessage( accessor ) );
    }

    private static String createMessage( AccessibleObject accessor )
    {
        return "Not allowed to subclass " + Property.class.getName() + ". Property accessor " +
               accessor + " is returning a Property subclass.";
    }
}
