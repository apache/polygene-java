package org.qi4j.samples.forum.domain;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
@Mixins(Forum.Mixin.class)
public interface Forum
{
    void changedName( String name );

    class Mixin
        implements Forum
    {
        @State
        public Property<String> name;

        @State
        public ManyAssociation<Board> boards;

        @Override
        public void changedName( String name )
        {
            this.name.set( name );
        }
    }
}
