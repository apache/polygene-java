package org.qi4j.entitystore.qrm.internal;

import java.util.Map;
import org.qi4j.entitystore.qrm.QrmEntityStoreDescriptor;

/**
 * User: alex
 */
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
