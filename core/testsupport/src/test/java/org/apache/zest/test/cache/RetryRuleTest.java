package org.apache.zest.test.cache;

import org.apache.zest.test.util.RetryRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RetryRuleTest
{
    @Rule public RetryRule retry = new RetryRule( 3 );

    static int count = 0;

    @Test
    public void test()
    {
        count++;
        assertThat( count, is( 3 ) );
    }
}
