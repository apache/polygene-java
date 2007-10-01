package org.qi4j.api.query;

/**
 * TODO
 */
public class BinaryQueryExpression
{
    QueryExpression left;
    QueryExpression right;

    public BinaryQueryExpression( QueryExpression left, QueryExpression right )
    {
        this.left = left;
        this.right = right;
    }

    public QueryExpression getLeft()
    {
        return left;
    }

    public QueryExpression getRight()
    {
        return right;
    }
}
