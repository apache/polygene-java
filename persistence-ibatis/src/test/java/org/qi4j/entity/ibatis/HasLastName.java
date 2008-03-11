package org.qi4j.entity.ibatis;

import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.property.Property;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
@Mixins( HasLastName.HasLastNameMixin.class )
public interface HasLastName
{
    Property<String> lastName();

    class HasLastNameMixin implements HasLastName
    {
        @PropertyField
        private Property<String> lastName;

        public Property<String> lastName()
        {
            return lastName;
        }
    }
}