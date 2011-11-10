/*  Copyright 2009 Alex Shneyderman
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
package org.qi4j.entitystore.qrm.internal;

import org.qi4j.entitystore.qrm.QrmEntityStoreDescriptor;

import java.util.Map;

public class MapperUtils
{

    public static String tableName( Class clazz, QrmEntityStoreDescriptor qrmCfg, Map<Class, QrmMapping> mappings )
    {
        String clazzName = clazz.getName();
        return clazzName.substring( clazzName.lastIndexOf( '.' ) + 1 ).toUpperCase();
    }

    public static String entityName( Class clazz, QrmEntityStoreDescriptor qrmCfg, Map<Class, QrmMapping> mappings )
    {
        return clazz.getName();
    }

    public static String idColumnName( Class clazz, QrmEntityStoreDescriptor qrmCfg, Map<Class, QrmMapping> mappings )
    {
        return tableName( clazz, qrmCfg, mappings ).toLowerCase() + "_id";
    }

    public static String columnName( String propName )
    {
        // TODO: implement;
        return propName;
    }
}
