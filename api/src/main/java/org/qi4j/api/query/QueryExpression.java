package org.qi4j.api.query;

/**
 * TODO
 */
public class QueryExpression
{
    public static <L extends R, R extends L> QueryExpression eq( L left, R right )
    {
        return null;
    }

    public static <L extends R, R extends L> QueryExpression ne( L left, R right )
    {
        return null;
    }

    public static <L extends R, R extends L> QueryExpression lt( L left, R right )
    {
        return null;
    }

    public static <L extends R, R extends L> QueryExpression gt( Comparable<L> left, Comparable<R> right )
    {
        return null;
    }

    public static <L extends R, R extends L> QueryExpression le( Comparable<L> left, Comparable<R> right )
    {
        return null;
    }

    public static <L extends R, R extends L> QueryExpression ge( Comparable<L> left, Comparable<R> right )
    {
        return null;
    }

    public static QueryExpression matches( String source, String expression )
    {
        return null;
    }

    public static <K> QueryExpression contains( Iterable<K> left, K right )
    {
        return null;
    }

    public static QueryExpression contains( String value, String substring )
    {
        return null;
    }

    public static QueryExpression isNull( Object value )
    {
        return null;
    }


    public static QueryExpression isNotNull( Object value )
    {
        return null;
    }

    public static QueryExpression and( QueryExpression... expressions )
    {
        return null;
    }

    public static QueryExpression or( QueryExpression... expressions )
    {
        return null;
    }

    public static QueryExpression not( QueryExpression expr )
    {
        return null;
    }
}
