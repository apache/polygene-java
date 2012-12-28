package org.qi4j.samples.forum.data.entity;

import java.util.Date;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public interface Post
    extends EntityComposite
{
    enum Status
    {
        POSTED,
        MODERATION,
        DELETED,
        SPAM
    }

    @Optional
    Association<Post> replyTo();

    Property<String> message();

    Association<User> poster();

    Property<Status> status();

    Property<User> createdBy();

    Property<Date> createdOn();
}
