package org.qi4j.entitystore.qrm.internal;

/**
 * User: alex
 */
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
