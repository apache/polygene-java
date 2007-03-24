/*
 * Copyright 2007 Edward Yakop.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.samples.common.generator;

import java.util.Random;
import org.ops4j.orthogon.mixin.IdGenerator;

public final class CommonIdGenerator
    implements IdGenerator
{
    private static final long SEED = System.currentTimeMillis();
    private static final Random RANDOM = new Random( SEED );

    public CommonIdGenerator()
    {
    }

    public String generateId( Class primaryAspect )
    {
        if( primaryAspect == null )
        {
            return null;
        }

        String classSimpleName = primaryAspect.getSimpleName();
        return classSimpleName + "-" + RANDOM.nextInt( 10000 );
    }
}
