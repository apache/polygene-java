package org.qi4j.samples.forum.domain;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

/**
 * TODO
 */
@Mixins(Board.Mixin.class)
public interface Board
{
    Topic createdTopic( String id );

    class Mixin
        implements Board
    {
        @State
        Property<String> name;

        @State
        ManyAssociation<Topic> topics;

        @Structure
        Module module;

        @Override
        public Topic createdTopic( String id )
        {
            Topic entity = module.currentUnitOfWork().newEntity( Topic.class, id );
            topics.add( entity );
            return entity;
        }
    }
}
