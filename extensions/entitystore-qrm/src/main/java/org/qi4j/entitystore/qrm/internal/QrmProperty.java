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

public interface QrmProperty
{

    String name();

    String hibernateType();

    boolean isNullable();

    boolean isIdentity();

    String column();

    public class Default
        implements QrmProperty
    {

        private String _name;
        private String _hibernateType;
        private String _column;
        private boolean _nullable;
        private boolean _isIdentity;

        public Default( String propName, String hibernateType, boolean nullable )
        {
            _name = propName;
            _hibernateType = hibernateType;
            _nullable = nullable;
            _column = MapperUtils.columnName( propName );
        }

        public Default( String propName, String hibernateType, boolean nullable, String column )
        {
            _name = propName;
            _hibernateType = hibernateType;
            _nullable = nullable;
            _column = column;
        }

        public Default( String propName, String hibernateType, boolean nullable, boolean isIdentity )
        {
            _name = propName;
            _hibernateType = hibernateType;
            _nullable = nullable;
            _column = MapperUtils.columnName( propName );
            _isIdentity = isIdentity;
        }

        public Default( String propName, String column, String hibernateType, boolean nullable, boolean isIdentity )
        {
            _name = propName;
            _hibernateType = hibernateType;
            _nullable = nullable;
            _column = column;
            _isIdentity = isIdentity;
        }

        public String name()
        {
            return _name;
        }

        public String hibernateType()
        {
            return _hibernateType;
        }

        public boolean isNullable()
        {
            return _nullable;
        }

        public boolean isIdentity()
        {
            return _isIdentity;
        }

        public String column()
        {
            return _column;
        }
    }
}
