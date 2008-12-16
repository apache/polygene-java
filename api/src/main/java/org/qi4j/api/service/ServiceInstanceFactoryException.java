/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.service;

/**
 * If a ServiceInstanceProvider could not create a service
 * instance it must throw this exception.
 */
public class ServiceInstanceFactoryException
    extends RuntimeException
{
    public ServiceInstanceFactoryException()
    {
    }

    public ServiceInstanceFactoryException( String string )
    {
        super( string );
    }

    public ServiceInstanceFactoryException( String string, Throwable throwable )
    {
        super( string, throwable );
    }

    public ServiceInstanceFactoryException( Throwable throwable )
    {
        super( throwable );
    }
}
