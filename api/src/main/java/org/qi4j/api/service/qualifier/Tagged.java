/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.service.qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Filter services based on tags. Tags can be set using the ServiceTags meta-info, like so:
 * module.addService(MyService.class).setMetaInfo(ServiceTags.tags("onetag","twotag"));
 *
 * and then at an injection point you can do this:
 *
 * @Service @Tagged("onetag") MyService service;
 * to get only a service tagged with MyService. If several match only the first match is used.
 */
@Retention( RetentionPolicy.RUNTIME)
@Qualifier( Tagged.TaggedQualifier.class )
public @interface Tagged
{
    public abstract String[] value();

    public final class TaggedQualifier
        implements AnnotationQualifier<Tagged>
    {
        public <T> ServiceQualifier qualifier( Tagged tagged )
        {
            return ServiceQualifier.withTags( tagged.value() );
        }
    }
}
