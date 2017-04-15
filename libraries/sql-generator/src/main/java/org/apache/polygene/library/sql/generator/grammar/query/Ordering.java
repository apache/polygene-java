package org.apache.polygene.library.sql.generator.grammar.query;

/**
 * This is enum for what kind of order will be applied to each ordering column. Can be either {@link #ASCENDING} or
 * {@link #DESCENDING}.
 *
 * @author Stanislav Muhametsin
 * @see SortSpecification
 */
public final class Ordering
{
    /**
     * The ordering will be ascending ({@code ASC}).
     */
    public static final Ordering ASCENDING = new Ordering();

    /**
     * The ordering will be descending ({@code DESC}).
     */
    public static final Ordering DESCENDING = new Ordering();
}
