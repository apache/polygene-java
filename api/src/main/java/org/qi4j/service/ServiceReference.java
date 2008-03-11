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

/**
 * From a ServiceReference you can access and modify metadata about a service.
 * You can also acquire a service instance through getInstance() that can be invoked. When the usage is
 * done this must be signalled by calling releaseInstance().
 */
public interface ServiceReference<T>
{
    <K extends Serializable> K getServiceInfo( Class<K> infoType );

    <K extends Serializable> void setServiceInfo( Class<K> infoType, K value );

    T getInstance()
        throws ServiceProviderException;

    void release()
        throws IllegalStateException;

}
