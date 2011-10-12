package org.qi4j.samples.forum.data.entity;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public interface Topic
    extends EntityComposite
{
    enum Status
    {
        POSTED,
        MODERATION,
        DELETED,
        SPAM
    }

    Property<String> subject();

    Property<Status> status();

    @Aggregated
    ManyAssociation<Post> posts();

    Association<Post> lastPost();

    Property<Integer> postCount();
}
