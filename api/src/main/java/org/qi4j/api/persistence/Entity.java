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
package org.qi4j.api.persistence;

import java.net.URL;

public interface Entity
{
    boolean isReference();

    /**
     * The full absolute external form of the composite reference.
     * <p/>
     * The composite reference will be converted to a URL with the following
     * format;
     * <code><pre>
     * &lt;transport-protocol&gt;://&lt;repository-host&gt;/&lt;identity&gt;?type=&lt;type&gt;
     * </pre></code>
     *
     * @return A URL pointing to the potenitally globally accessible location where the composite
     *         can be retrieved.
     */
    URL toURL();
}
