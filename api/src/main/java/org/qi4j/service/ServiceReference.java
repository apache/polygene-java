/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.service;

import java.io.Serializable;
import org.qi4j.property.ImmutableProperty;

/**
 * From a ServiceReference you can access and modify metadata about a service.
 * You can also the actual service through getService(), that can then be invoked. When the usage is
 * done this must be signalled by calling releaseService().
 */
public interface ServiceReference<T>
{
    ImmutableProperty identity();

    <K extends Serializable> K getServiceAttribute( Class<K> infoType );

    <K extends Serializable> void setServiceAttribute( Class<K> infoType, K value );

    T get();

    void releaseService()
        throws IllegalStateException;
}
