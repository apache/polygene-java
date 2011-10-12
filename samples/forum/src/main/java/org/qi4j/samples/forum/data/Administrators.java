package org.qi4j.samples.forum.data;

import org.qi4j.api.association.ManyAssociation;
import org.qi4j.samples.forum.data.entity.User;

/**
 * TODO
 */
public interface Administrators
{
    ManyAssociation<User> administrators();
}
