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
 */
package org.apache.polygene.entitystore.jooq;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.util.Classes;
import org.jooq.CreateTableColumnStep;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class TypesTable
    implements TableFields
{
    private final Map<Class<?>, Table<Record>> mixinTablesCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Table<Record>> mixinAssocsTablesCache = new ConcurrentHashMap<>();

    private final Table<Record> typesTable;
    private final SQLDialect dialect;
    private final Schema schema;

    private final JooqDslContext dsl;

    TypesTable( JooqDslContext dsl, Schema schema,
                SQLDialect dialect,
                String typesTablesName
              )
    {
        this.schema = schema;
        this.dialect = dialect;
        typesTable = tableOf( typesTablesName );
        this.dsl = dsl;
    }

    static <T> Field<T> makeField( String columnName, Class<T> type )
    {
        return DSL.field( DSL.name( columnName ), type );
    }

    Table<Record> tableOf( String tableName )
    {
        return DSL.table(
            dialect.equals( SQLDialect.SQLITE )
            ? DSL.name( tableName )
            : DSL.name( schema.getName(), tableName ) );
    }

    String tableNameOf( Class<?> mixinType )
    {
        Result<Record> typeInfo = fetchTypeInfoFromTable( mixinType );
        if( typeInfo.isEmpty() )
        {
            return null;
        }
        return typeInfo.getValue( 0, tableNameColumn );
    }

    Table<Record> tableFor( Class<?> type, EntityDescriptor descriptor )
    {
        return mixinTablesCache.computeIfAbsent( type, t ->
        {
            String tableName = tableNameOf( t );
            if( tableName == null )
            {
                Result<Record> newMixinTable = createNewMixinTable( type, descriptor );
                return tableOf( newMixinTable.getValue( 0, tableNameColumn ) );
            }
            return tableOf( tableName );
        } );
    }

    private Result<Record> fetchTypeInfoFromTable( Class<?> mixinTableName )
    {
        return dsl.select()
                  .from( typesTable )
                  .where( identityColumn.eq( mixinTableName.getName() ) )
                  .fetch();
    }

    private Result<Record> createNewMixinTable( Class<?> mixinType, EntityDescriptor descriptor )
    {
        String mixinTypeName = mixinType.getName();
        String tableName = createNewTableName( mixinType );
        CreateTableColumnStep primaryTable = dsl.createTable( DSL.name( schema.getName(), tableName ) )
                                                .column( identityColumn )
                                                .column( createdColumn );
        descriptor.state().properties().forEach(
            property ->
            {
                QualifiedName qualifiedName = property.qualifiedName();
                if( qualifiedName.type().replace( '-', '$' ).equals( mixinTypeName ) )
                {
                    primaryTable.column( fieldOf( property ) );
                }
            } );
        descriptor.state().associations().forEach(
            assoc ->
            {
                QualifiedName qualifiedName = assoc.qualifiedName();
                if( qualifiedName.type().replace( '-', '$' ).equals( mixinTypeName ) )
                {
                    primaryTable.column( fieldOf( assoc ) );
                }
            } );
        int result1 = primaryTable.execute();
        int result3 = dsl.insertInto( typesTable )
                         .set( identityColumn, mixinTypeName )
                         .set( tableNameColumn, tableName )
                         .set( createdColumn, new Timestamp( System.currentTimeMillis() ) )
                         .set( modifiedColumn, new Timestamp( System.currentTimeMillis() ) )
                         .execute();
        return fetchTypeInfoFromTable( mixinType );
    }

    private String createNewTableName( Class<?> mixinType )
    {
        String typeName = mixinType.getSimpleName();
        String postFix = "";
        int counter = 0;
        boolean found = false;
        do
        {
            found = checkForTableNamed( typeName + postFix );
            postFix = "_" + counter++;
        } while( found );
        return typeName;
    }

    private boolean checkForTableNamed( String tableName )
    {
        return dsl.select()
                  .from( typesTable )
                  .where( tableNameColumn.eq( tableName ) )
                  .fetch().size() > 0;
    }

    private boolean isProperty( Method method )
    {
        return Property.class.isAssignableFrom( method.getReturnType() ) && method.getParameterCount() == 0;
    }

    Field<Object> fieldOf( PropertyDescriptor descriptor )
    {
        String propertyName = descriptor.qualifiedName().name();
        return DSL.field( DSL.name( propertyName ), dataTypeOf( descriptor ) );
    }

    Field<String> fieldOf( AssociationDescriptor descriptor )
    {
        String propertyName = descriptor.qualifiedName().name();
        return DSL.field( DSL.name( propertyName ), DSL.getDataType( String.class ) );
    }

    private <T> DataType<T> dataTypeOf( PropertyDescriptor property )
    {
        Type type = property.type();
        Class<?> rawType = Classes.RAW_CLASS.apply( type );
        return SqlType.getSqlDataTypeFor( rawType );
    }
}
