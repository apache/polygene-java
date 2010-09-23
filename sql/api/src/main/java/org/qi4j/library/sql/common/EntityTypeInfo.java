/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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


package org.qi4j.library.sql.common;

import org.qi4j.spi.entity.EntityDescriptor;

/**
 * A helper interface to encapsulate information about certain entity type and how it appears in database.
 *
 * @author Stanislav Muhametsin
 */
public final class EntityTypeInfo
{
   private Integer _entityTypeID;

   private EntityDescriptor _entityDescriptor;

   public EntityTypeInfo(EntityDescriptor entityDescriptor, Integer entityTypeID)
   {
      this._entityTypeID = entityTypeID;
      this._entityDescriptor = entityDescriptor;
   }

   /**
    * Gets the primary key of this entity type, as it is stored in database in entity type lookup table.
    * @return The primary key of this entity type, as it is stored in database in entity type lookup table.
    */
   public Integer getEntityTypePK()
   {
      return this._entityTypeID;
   }

   /**
    * Gets the entity descriptor of this entity type.
    * @return The entity descriptor of this entity type.
    */
   public EntityDescriptor getEntityDescriptor()
   {
      return this._entityDescriptor;
   }

}
