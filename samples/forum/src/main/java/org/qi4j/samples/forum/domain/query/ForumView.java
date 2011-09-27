package org.qi4j.samples.forum.domain.query;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.samples.forum.domain.Board;
import org.qi4j.samples.forum.domain.Forum;

/**
* TODO
*/
public interface ForumView
    extends Forum
{
    Property<Integer> topicCount();
    Property<Integer> postCount();
}
