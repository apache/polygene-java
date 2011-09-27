package org.qi4j.samples.forum.domain.query;

import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.samples.forum.domain.Board;
import org.qi4j.samples.forum.domain.Post;

/**
* TODO
*/
public interface BoardView
    extends Board
{
    @UseDefaults
    Property<Integer> postCount();

    @UseDefaults
    Property<Integer> topicCount();

    @Optional
    Association<Post> lastPost();
}
