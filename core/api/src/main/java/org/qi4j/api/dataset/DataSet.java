package org.qi4j.api.dataset;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * definition.constrain(entity(Person.class))
 * builder.from(path(Person.class,Movie.))
 * TODO
 */
public interface DataSet<T>
{
    DataSet<T> constrain( Predicate<T> selection );

    <U> DataSet<U> project( Function<T, U> conversion );

    Query<T> newQuery();
}
