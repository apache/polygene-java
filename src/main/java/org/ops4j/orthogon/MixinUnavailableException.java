/*  Copyright 2007 Niclas Hedhman.
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
package org.ops4j.orthogon;

public class MixinUnavailableException extends RuntimeException
{
    private Class m_invokedOn;

    public MixinUnavailableException( Class invokedOn )
    {
        super( "No Mixin has been registered for " + invokedOn );
        m_invokedOn = invokedOn;
    }

    public Class getInvokedOn()
    {
        return m_invokedOn;
    }
}
