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


package org.qi4j.index.sql.common;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qi4j.api.common.QualifiedName;

/**
 * A helper interface to encapsulate information about qualified name and how it appears in database.
 * 
 * @author Stanislav Muhametsin
 */
public interface QNameInfo
{
   /**
    * Currently all possible types of qualified names: {@link #PROPERTY} for properties, {@link #ASSOCIATION} for associations, and {@link #MANY_ASSOCIATION} for many-associations.
    */
   public enum QNameType { PROPERTY, ASSOCIATION, MANY_ASSOCIATION };
   
   /**
    * Gets the qualified name this interface represents.
    * @return The qualified name this interface represents.
    * @see QualifiedName
    */
   QualifiedName getQName();
   
   /**
    * Gets the type of represented qualified name: either {@link QNameType#PROPERTY} for properties, {@link QNameType#ASSOCIATION} for associations, or {@link QNameType#MANY_ASSOCIATION} for many-associations.
    * @return The type of represented qualified name: either {@link QNameType#PROPERTY}, {@link QNameType#ASSOCIATION}, or {@link QNameType#MANY_ASSOCIATION}.
    */
   QNameType getQNameType();
   
   /**
    * If qualified name represented by this interface is a property with collection as type, returns the {@code amount of nested collections + 1}.
    * That is, assuming {@code X} is not a collection, for type {@code Property<Set<X>>} this returns {@code 1}, for {@code Property<Set<Set<X>>>} this returns {@code 2}, etc.
    * If qualified name represented by this interface is not a property or a property with no collection type, this method returns {@code 0}.
    * @return The collection depth ({@code > 0}) of qualified name, if this interface represents qualified name with collection property; {@code 0} otherwise.
    */
   Integer getCollectionDepth();
   
   /**
    * Returns the non-collection type of this qualified name. That is, for {@code Property<X>} this returns {@code X} if {@code X}
    * is not a collection type, and for {@code Property<Set<Y>>} this returns {@code Y} if {@code Y} is not a collection type.
    * @return The non-collection type of this qualified name.
    */
   Type getFinalType();
   
   /**
    * Returns whether the final (non-collection) type of this qualified name is not seen as value composite. Always returns {@code false} for qualified names of type {@link QNameType#ASSOCIATION} and {@link QNameType#MANY_ASSOCIATION}. 
    * @return {@code true} if {@link #getFinalType()} is not seen as value composite type; {@code false} otherwise.
    */
   Boolean isFinalTypePrimitive();

   /**
    * Gets the table name in database, used to store values of the qualified name this interface represents.
    * @return The table name in database, used to store values of the qualified name this interface represents.
    */
   String getTableName();

}
