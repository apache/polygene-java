/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entity;

public class EntityNotFoundException extends EntityStoreException
{
    private String storeName;
    private String identity;

    public EntityNotFoundException( String storeName, String identity )
    {
        this.storeName = storeName;
        this.identity = identity;
    }

    public String getStoreName()
    {
        return storeName;
    }

    public String getIdentity()
    {
        return identity;
    }

    public String getMessage()
    {
        return "Entity " + identity + " not found in the '" + storeName + "' store.";
    }
}
