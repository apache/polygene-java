package org.qi4j.entity.ibatis;

import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.property.Property;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
@Mixins( HasFirstName.HasFirstNameMixin.class )
public interface HasFirstName
{
    Property<String> firstName();

    class HasFirstNameMixin implements HasFirstName
    {
        @PropertyField( optional = false )
        private Property<String> firstName;

        public Property<String> firstName()
        {
            return firstName;
        }
    }
}