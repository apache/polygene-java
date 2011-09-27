package org.qi4j.samples.forum.domain.query;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.samples.forum.domain.Post;
import org.qi4j.samples.forum.domain.Topic;

/**
* TODO
*/
public interface TopicView
    extends Topic
{
    Association<Post> lastPost();

    Property<Integer> postCount();

    ManyAssociation<Post> posts();
}
