package org.apache.zest.test.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule to retry tests.
 * Useful for flaky tests.
 */
public class RetryRule implements TestRule
{
    private static final int MAX_DEFAULT = 3;

    private final int max;
    private int attempts = 0;

    public RetryRule()
    {
        this( MAX_DEFAULT );
    }

    public RetryRule( int max )
    {
        this.max = max;
    }

    @Override
    public Statement apply( Statement base, Description description )
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                attempts++;
                try
                {
                    base.evaluate();
                }
                catch( Throwable ex )
                {
                    if( attempts <= max )
                    {
                        evaluate();
                    }
                    else
                    {
                        throw ex;
                    }
                }
            }
        };
    }
}
