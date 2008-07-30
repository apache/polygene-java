/*
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.transaction;

import org.junit.Test;
import org.junit.Assert;
import org.qi4j.library.transaction.Transactional;

public class PropagationTest
{

    @Test
    public void ensurePublicApi()
    {
        // if an enum value is changed or removed the test wont compile
        // if a value is added will fail
        for( Transactional.Propagation propagation : Transactional.Propagation.values() )
        {
            switch( propagation )
            {
            case MANDATORY:
            case NEVER:
            case NOT_SUPPORTED:
            case REQUIRED:
            case REQUIRES_NEW:
            case SUPPORTS:
                break;
            default:
                Assert.fail( "unsupported type: " + propagation );
            }
        }
    }

}
