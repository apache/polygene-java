/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.jini.javaspaces;

import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;
import org.qi4j.composite.Optional;

public interface OutriggerConfiguration extends EntityComposite
{
    @Optional
    Property<Boolean> useJrmp();

    @Optional
    Property<String> hostInterface();

    @Optional
    Property<String> securityPolicy();

    @Optional
    Property<String> outriggerDlJarLocation();

    /** The groups that this Reggie instance should be serving.
     * This is a comma separated list of strings.
     *
     * @return the groups that Reggie is serving.
     */
    @Optional
    Property<String> groups();
}
