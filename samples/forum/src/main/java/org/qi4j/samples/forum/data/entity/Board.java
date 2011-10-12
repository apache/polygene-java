package org.qi4j.samples.forum.data.entity;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.samples.forum.data.Moderators;

/**
 * TODO
 */
public interface Board
    extends Moderators, EntityComposite
{
    Property<String> name();

    @Aggregated
    ManyAssociation<Topic> topics();

    @UseDefaults
    Property<Integer> postCount();

    @UseDefaults
    Property<Integer> topicCount();

    @Optional
    Association<Post> lastPost();
}
