package org.qi4j.api.dataset;

import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;

/**
 * definition.constrain(entity(Person.class))
 * builder.from(path(Person.class,Movie.))
 * TODO
 */
public interface DataSet<T>
{
    DataSet<T> constrain( Specification<T> selection );

    <U> DataSet<U> project( Function<T, U> conversion );

    Query<T> newQuery();
}
