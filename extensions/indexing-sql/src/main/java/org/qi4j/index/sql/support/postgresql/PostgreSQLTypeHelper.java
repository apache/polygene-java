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

package org.qi4j.index.sql.support.postgresql;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.index.sql.support.skeletons.SQLDBState;

/**
 * A little helper interface to convert java types to SQL types when inserting objects to prepared statements. It will
 * first preprocess the object and possibly convert it to something else (like Character to Integer). Then it will use
 * the {@link SQLDBState#javaTypes2SQLTypes()} mapping to determine which SQL datatype to use when inserting the
 * object to prepared statement.
 *
 * @author Stanislav Muhametsin
 */
@Mixins(
{
    PostgreSQLTypeHelper.SQLTypeHelperMixin.class
})
public interface PostgreSQLTypeHelper
{

    /**
     * Adds the specified object at specified index in specified prepared statement.
     *
     * @param ps The prepared statement.
     * @param index The index for the object to be inserted in prepared statemtent ({@code > 0}).
     * @param primitive The object to insert.
     * @param primitiveType The type of object.
     * @throws SQLException If something underlying throws it.
     */
    void addPrimitiveToPS( PreparedStatement ps, Integer index, @Optional Object primitive, Type primitiveType )
        throws SQLException;

    Integer getSQLType( Object primitive );

    public class SQLTypeHelperMixin
        implements PostgreSQLTypeHelper
    {

        @This
        private SQLDBState _state;

        public Object processJavaPrimitiveBeforeUsingInStatement( Object primitive )
        {
            if( primitive != null )
            {
                if( primitive instanceof Character )
                {
                    primitive = Character.codePointAt( new char[]
                    {
                        (Character) primitive
                    }, 0 );
                }
                else if( primitive instanceof Date )
                {
                    primitive = new Timestamp( ((Date) primitive).getTime() );
                }
                else if( primitive instanceof Byte )
                {
                    primitive = (short) (Byte) primitive;
                }
                else if( primitive instanceof Short )
                {
                    primitive = (int) (Short) primitive;
                }
            }
            return primitive;
        }

        @Override
        public void addPrimitiveToPS( PreparedStatement ps, Integer index, Object primitive, Type primitiveType )
            throws SQLException
        {
            primitive = processJavaPrimitiveBeforeUsingInStatement( primitive );
            Integer sqlType = this.getSQLType( primitiveType );
            ps.setObject( index, primitive, sqlType );
        }

        @Override
        public Integer getSQLType( Object primitive )
        {
            primitive = processJavaPrimitiveBeforeUsingInStatement( primitive );

            return this.getSQLType( primitive.getClass() );
        }

        private Integer getSQLType( Type primitiveType )
        {
            if( primitiveType instanceof ParameterizedType )
            {
                primitiveType = ((ParameterizedType) primitiveType).getRawType();
            }

            Class<?> primitiveClass = (Class<?>) primitiveType;

            if( Enum.class.isAssignableFrom( primitiveClass ) )
            {
                primitiveClass = Enum.class;
            }
            Integer sqlType = this._state.javaTypes2SQLTypes().get().get( primitiveClass );
            if( sqlType == null )
            {
                throw new InternalError( "Could not find mapping from " + primitiveClass + " to SQL type." );
            }

            return sqlType;
        }
    }
}