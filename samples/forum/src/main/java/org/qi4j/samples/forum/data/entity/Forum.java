package org.qi4j.samples.forum.data.entity;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.samples.forum.data.Administrators;
import org.qi4j.samples.forum.data.Moderators;

/**
 * TODO
 */
public interface Forum
    extends Moderators, Administrators, EntityComposite
{
    public Property<String> name();

    @Aggregated
    ManyAssociation<Board> boards();

    Property<Integer> topicCount();

    Property<Integer> postCount();
}
