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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.qi4j.api.common.QualifiedName;

/**
 * A helper interface to encapsulate information about qualified name and how it appears in database.
 *  
 * See {@link #fromProperty(QualifiedName, List, String, Type, String)}, {@link #fromAssociation(QualifiedName, String, Type)} and {@link #fromManyAssociation(QualifiedName, String, Type)} methods in order to instantiate this class.
 * 
 * @author Stanislav Muhametsin
 */
public final class QNameInfo
{
   /**
    * Currently all possible types of qualified names: {@link #PROPERTY} for properties, {@link #ASSOCIATION} for associations, and {@link #MANY_ASSOCIATION} for many-associations.
    */
   public enum QNameType { PROPERTY, ASSOCIATION, MANY_ASSOCIATION };
   
   private static final List<Class<?>> EMPTY_COLL_CLASSES = new ArrayList<Class<?>>();
   
   private String _tableName;
   
   /**
    * Store also information about collection types - in case it is required in future.
    */
   private final List<Class<?>> _collectionClasses;
   
   private final QualifiedName _qName;
   
   private final QNameType _qNameType;
   
   private final Type _finalType;
   
   private final Boolean _isFinalTypePrimitive;
   
   private QNameInfo(
         QualifiedName qName, //
         QNameType qNameType, //
         List<Class<?>> collectionClasses, //
         String tableName, //
         Type finalType //
         )
   {
      this._qName = qName;
      this._qNameType = qNameType;
      this._collectionClasses = (collectionClasses == null  || collectionClasses.size() == 0 ? EMPTY_COLL_CLASSES : collectionClasses);
      this._tableName = tableName;
      this._finalType = finalType;
      Boolean isFinalTypePrimitive = false;
      if (finalType instanceof Class<?>)
      {
         Class<?> finalClass = (Class<?>)finalType;
         isFinalTypePrimitive = //
            Number.class.isAssignableFrom(finalClass)//
            || Boolean.class.isAssignableFrom(finalClass) //
            || Character.class.isAssignableFrom(finalClass) //
            || Date.class.isAssignableFrom(finalClass) //
            || Enum.class.isAssignableFrom(finalClass) //
            || String.class.isAssignableFrom(finalClass)//
            ;
      }
      this._isFinalTypePrimitive = isFinalTypePrimitive;
   }
   
   /**
    * If qualified name represented by this interface is a property with collection as type, returns the {@code amount of nested collections + 1}.
    * That is, assuming {@code X} is not a collection, for type {@code Property<Set<X>>} this returns {@code 1}, for {@code Property<Set<Set<X>>>} this returns {@code 2}, etc.
    * If qualified name represented by this interface is not a property or a property with no collection type, this method returns {@code 0}.
    * @return The collection depth ({@code > 0}) of qualified name, if this interface represents qualified name with collection property; {@code 0} otherwise.
    */
   public Integer getCollectionDepth()
   {
      return this._collectionClasses.size();
   }
   
   /**
    * Returns the non-collection type of this qualified name. That is, for {@code Property<X>} this returns {@code X} if {@code X}
    * is not a collection type, and for {@code Property<Set<Y>>} this returns {@code Y} if {@code Y} is not a collection type.
    * @return The non-collection type of this qualified name.
    */
   public Type getFinalType()
   {
      return this._finalType;
   }
   
   /**
    * Gets the qualified name this interface represents.
    * @return The qualified name this interface represents.
    * @see QualifiedName
    */
   public QualifiedName getQName()
   {
      return this._qName;
   }
   
   /**
    * Gets the table name in database, used to store values of the qualified name this interface represents.
    * @return The table name in database, used to store values of the qualified name this interface represents. May be {@code null} if it is not yet decided.
    */
   public String getTableName()
   {
      return this._tableName;
   }
   
   /**
    * Sets the previously undecided table name to some specific one. This method only works when argument is {@code non-null} and current table name is {@code null}.
    * @param tableName The new table name. Must be {@code non-null}.
    * @throws IllegalArgumentException If {@code tableName} is {@code null}.
    * @throws IllegalStateException If current table name is {@code non-null}.
    */
   public void setTableName(String tableName)
   {
      if (tableName == null)
      {
         throw new IllegalArgumentException("Table name must not be null.");
      }
      if (this._tableName == null)
      {
         this._tableName = tableName;
      } else
      {
         throw new IllegalStateException("Can only set table name when it is null.");
      }
   }

   /**
    * Returns whether the final (non-collection) type of this qualified name is not seen as value composite. Always returns {@code false} for qualified names of type {@link QNameType#ASSOCIATION} and {@link QNameType#MANY_ASSOCIATION}. 
    * @return {@code true} if {@link #getFinalType()} is not seen as value composite type; {@code false} otherwise.
    */
   public Boolean isFinalTypePrimitive()
   {
      return this._isFinalTypePrimitive;
   }
   
   /**
    * Gets the type of represented qualified name: either {@link QNameType#PROPERTY} for properties, {@link QNameType#ASSOCIATION} for associations, or {@link QNameType#MANY_ASSOCIATION} for many-associations.
    * @return The type of represented qualified name: either {@link QNameType#PROPERTY}, {@link QNameType#ASSOCIATION}, or {@link QNameType#MANY_ASSOCIATION}.
    */
   public QNameType getQNameType()
   {
      return this._qNameType;
   }
   
   /**
    * Creates information about specified qualified name which represents a property.
    * @param qName The qualified name of property.
    * @param collectionClasses Possible collection classes, with outermost collection type being first in list, and innermost collection type last in list. May be {@code null} or empty list.
    * @param tableName The table name where the values of all instances of propertiy with this qualified name will be stored. May be {@code null} if it is to be decided later.
    * @param finalType The non-collection type of this property.
    * @return An object representing information about property with this qualified name, and how instances of this property are stored in database.
    */
   public static QNameInfo fromProperty( //
         QualifiedName qName, //
         List<Class<?>> collectionClasses, //
         String tableName, //
         Type finalType
         )
   {
      return new QNameInfo(
            qName,
            QNameType.PROPERTY,
            collectionClasses,
            tableName,
            finalType
            );
   }
   
   /**
    * Creates information about specified qualified name which represents an association.
    * @param qName The qualified name of the association.
    * @param tableName The table name where the values of all instances of association with this qualified name will be stored. May be {@code null} if it is to be decided later.
    * @param assoTargetType The target type of the association.
    * @return An object representing information about association with this qualified name, and how instances of this association are stored in database.
    */
   public static QNameInfo fromAssociation( //
         QualifiedName qName, //
         String tableName, //
         Type assoTargetType //
         )
   {
      return new QNameInfo(
            qName,
            QNameType.ASSOCIATION,
            null,
            tableName,
            assoTargetType
            );
   }
   
   /**
    * Creates information about specified qualified name which represents a many-association.
    * @param qName The qualified name of the many-association.
    * @param tableName The table name where the values of all instances of many-association with this qualified name will be stored. May be {@code null} if it is to be decided later.
    * @param assoTargetType The target type of the many-association.
    * @return An object representing information about many-association with this qualified name, and how instances of this many-association are stored in database.
    */
   public static QNameInfo fromManyAssociation( //
         QualifiedName qName, //
         String tableName, //
         Type assoTargetType
         )
   {
      return new QNameInfo(
            qName,
            QNameType.MANY_ASSOCIATION,
            null,
            tableName,
            assoTargetType
            );
   }
   
}

