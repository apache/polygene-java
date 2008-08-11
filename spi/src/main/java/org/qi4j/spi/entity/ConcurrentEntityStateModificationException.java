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

import java.util.Collection;

/**
 * This exception should be thrown from {@link EntityStore#prepare(Iterable, Iterable, Iterable)} if the EntityStore
 * detects that the entities being saved have been changed since they were created.
 */
public class ConcurrentEntityStateModificationException extends EntityStoreException
{
    private String storeName;
    private Collection<QualifiedIdentity> modifiedEntities;

    public ConcurrentEntityStateModificationException( String storeName, Collection<QualifiedIdentity> modifiedEntities )
    {
        this.storeName = storeName;
        this.modifiedEntities = modifiedEntities;
    }

    public String storeId()
    {
        return storeName;
    }

    public Collection<QualifiedIdentity> modifiedEntities()
    {
        return modifiedEntities;
    }

    public String getMessage()
    {
        return "Entities changed concurrently in the '" + storeName + "' store:" + modifiedEntities;
    }
}