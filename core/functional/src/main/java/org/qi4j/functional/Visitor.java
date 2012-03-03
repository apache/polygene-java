package org.qi4j.functional;

/**
 * Generic Visitor interface.
 */
public interface Visitor<T, ThrowableType extends Throwable>
{
    /**
     * Visit an instance of T
     *
     * @param visited the visited instance
     *
     * @return true if the visitor pattern should continue, false if it should be aborted
     *
     * @throws ThrowableType if an exception occurred during processing. Any client call that initiated the visiting should
     *                       get the exception in order to handle it properly.
     */
    boolean visit( T visited )
        throws ThrowableType;
}
