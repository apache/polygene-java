package org.qi4j.runtime.query.model;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
@Mixins( Describable.Mixin.class )
public interface Describable
{
    String getDescription();

    void updateDescription( String newDescription );

    class Mixin
        implements Describable
    {
        @State
        @UseDefaults
        public Property<String> description;

        @Override
        public String getDescription()
        {
            return description.get();
        }

        @Override
        public void updateDescription( String newDescription )
        {
            description.set( newDescription );
        }
    }
}
