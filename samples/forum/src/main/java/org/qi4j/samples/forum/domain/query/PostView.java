package org.qi4j.samples.forum.domain.query;

import org.qi4j.api.property.Property;
import org.qi4j.samples.forum.domain.Post;
import org.qi4j.samples.forum.domain.User;

import java.util.Date;

/**
 * TODO
 */
public interface PostView
    extends Post
{
    Property<User> createdBy();

    Property<Date> createdOn();
}
