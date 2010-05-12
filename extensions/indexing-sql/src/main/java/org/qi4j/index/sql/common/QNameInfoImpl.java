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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qi4j.api.common.QualifiedName;



/**
 * Implementation of the {@link QNameInfo}. See {@link #fromProperty(QualifiedName, List, String, Type, String)}, {@link #fromAssociation(QualifiedName, String, Type)} and {@link #fromManyAssociation(QualifiedName, String, Type)} methods in order to instantiate this class.
 * 
 * @author Stanislav Muhametsin
 */
public class QNameInfoImpl implements QNameInfo
{
   
   private static final List<Class<?>> EMPTY_COLL_CLASSES = new ArrayList<Class<?>>();
   
   private final String _tableName;
   
   /**
    * Store also information about collection types - in case it is required in future.
    */
   private final List<Class<?>> _collectionClasses;
   
   private final QualifiedName _qName;
   
   private final QNameType _qNameType;
   
   private final Type _finalType;
   
   private final Boolean _isFinalTypePrimitive;
   
   private final Set<String> _entityTypesUsingThisQName;
   
   private QNameInfoImpl(
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
      //System.out.println("QName: " + qName + ", type: " + qNameType + ", table name: " + tableName + ", finalType: " + finalType + ", isPrimitive: " + this._isFinalTypePrimitive + ", collectionClasses: " + collectionClasses);
      this._entityTypesUsingThisQName = new HashSet<String>();
   }
   
   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#getCollectionDepth()
    */
   @Override
   public Integer getCollectionDepth()
   {
      return this._collectionClasses.size();
   }
   
   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#getReferencedQNames()
    */
   
   public void addEntityTypeUsingThisQName(String entityType)
   {
      this._entityTypesUsingThisQName.add(entityType);
   }
   
   
   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#getFinalType()
    */
   @Override
   public Type getFinalType()
   {
      return this._finalType;
   }
   
   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#getQName()
    */
   @Override
   public QualifiedName getQName()
   {
      return this._qName;
   }
   
   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#getTableName(java.lang.Integer)
    */
   @Override
   public String getTableName()
   {
      return this._tableName;
   }

   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#isFinalTypePrimitive()
    */
   @Override
   public Boolean isFinalTypePrimitive()
   {
      return this._isFinalTypePrimitive;
   }
   
   /* (non-Javadoc)
    * @see org.qi4j.library.sql.jdbc.QNameInfo#getQNameType()
    */
   @Override
   public QNameType getQNameType()
   {
      return this._qNameType;
   }
   
   /**
    * Creates information about specified qualified name which represents a property.
    * @param qName The qualified name of property.
    * @param collectionClasses Possible collection classes, with outermost collection type being first in list, and innermost collection type last in list. May be {@code null} or empty list.
    * @param tableName The table name where the values of all instances of propertiy with this qualified name will be stored.
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
      return new QNameInfoImpl(
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
    * @param tableName The table name where the values of all instances of association with this qualified name will be stored.
    * @param assoTargetType The target type of the association.
    * @return An object representing information about association with this qualified name, and how instances of this association are stored in database.
    */
   public static QNameInfo fromAssociation( //
         QualifiedName qName, //
         String tableName, //
         Type assoTargetType //
         )
   {
      return new QNameInfoImpl(
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
    * @param tableName The table name where the values of all instances of many-association with this qualified name will be stored.
    * @param assoTargetType The target type of the many-association.
    * @return An object representing information about many-association with this qualified name, and how instances of this many-association are stored in database.
    */
   public static QNameInfo fromManyAssociation( //
         QualifiedName qName, //
         String tableName, //
         Type assoTargetType
         )
   {
      return new QNameInfoImpl(
            qName,
            QNameType.MANY_ASSOCIATION,
            null,
            tableName,
            assoTargetType
            );
   }
   
}
