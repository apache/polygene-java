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
package org.apache.polygene.spi.entitystore;

import java.util.Collection;
import org.apache.polygene.api.entity.EntityReference;

/**
 * This exception should be thrown if the EntityStore detects that the entities being saved have been changed
 * since they were created.
 */
public class ConcurrentEntityStateModificationException
    extends EntityStoreException
{
    private static final String NL = System.getProperty( "line.separator" );

    private Collection<EntityReference> modifiedEntities;

    public ConcurrentEntityStateModificationException( Collection<EntityReference> modifiedEntities )
    {
        super();
        this.modifiedEntities = modifiedEntities;
    }

    public Collection<EntityReference> modifiedEntities()
    {
        return modifiedEntities;
    }

    @Override
    public String getMessage()
    {
        return "Entities changed concurrently." + NL + "Modified entities are;" + NL + modifiedEntities;
    }
}