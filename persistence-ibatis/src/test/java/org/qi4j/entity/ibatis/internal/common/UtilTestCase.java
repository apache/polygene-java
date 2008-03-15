/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis.internal.common;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import static org.qi4j.entity.ibatis.internal.common.Util.isNotEquals;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class UtilTestCase
{
    /**
     * Tests not equals.
     *
     * @since 0.1.0
     */
    @Test
    public final void testNotEquals()
    {
        assertFalse( isNotEquals( null, null ) );
        assertFalse( isNotEquals( 1, 1 ) );
        assertTrue( isNotEquals( null, 1 ) );
        assertTrue( isNotEquals( 1, null ) );
        assertTrue( isNotEquals( 1, 2 ) );
    }
}
