package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Query;
import org.qi4j.api.persistence.QueryFactory;

/**
 * TODO
 */
public interface QueryByStringFactory
    extends QueryFactory

{
    <T> Query<T> newQuery(Class<T> resultType, String query);
}
