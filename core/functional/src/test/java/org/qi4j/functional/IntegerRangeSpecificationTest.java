/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.functional;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// This test exist primarily for the documentation. Don't remove.
public class IntegerRangeSpecificationTest
{
    @Test
    public void test1()
    {
        Specification<Integer> spec = new IntegerRangeSpecification( 10, 12 );
        assertTrue( spec.satisfiedBy( 10 ) );
        assertTrue( spec.satisfiedBy( 11 ) );
        assertTrue( spec.satisfiedBy( 12 ) );
        assertFalse( spec.satisfiedBy( 9 ) );
        assertFalse( spec.satisfiedBy( 13 ) );
    }

    // START SNIPPET: specification
    public static class IntegerRangeSpecification
        implements Specification<Integer>
    {

        private int lower;
        private int higher;

        public IntegerRangeSpecification( int lower, int higher )
        {
            this.lower = lower;
            this.higher = higher;
        }

        @Override
        public boolean satisfiedBy( Integer item )
        {
            return item >= lower && item <= higher;
        }
    }
    // END SNIPPET: specification
}
