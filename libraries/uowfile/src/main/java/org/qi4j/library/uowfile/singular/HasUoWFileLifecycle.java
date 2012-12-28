/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.uowfile.singular;

import java.io.File;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

@Mixins( HasUoWFileLifecycle.Mixin.class )
public interface HasUoWFileLifecycle
        extends HasUoWFile, Lifecycle
{

    class Mixin
            implements Lifecycle
    {

        @This
        private HasUoWFile hasUoWFile;

        @Override
        public void create()
                throws LifecycleException
        {
            // NOOP
        }

        @Override
        public void remove()
                throws LifecycleException
        {
            // We use the managed file so that if the UoW gets discarded the file will be restored
            File file = hasUoWFile.managedFile();
            if ( file.exists() && !file.delete() ) {
                throw new LifecycleException( "Unable to delete existing file: " + file );
            }
        }

    }

}
