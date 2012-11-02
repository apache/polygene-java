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
package org.qi4j.library.uowfile.plural;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

@Mixins( HasUoWFilesLifecycle.Mixin.class )
public interface HasUoWFilesLifecycle<T extends Enum<T>>
        extends HasUoWFiles<T>, Lifecycle
{

    public class Mixin
            implements Lifecycle
    {

        @This
        private HasUoWFiles<?> hasUoWFiles;

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
            // We use the managed files so that if the UoW gets discarded the files will be restored
            List<File> errors = new ArrayList<File>();
            for ( File eachFile : hasUoWFiles.managedFiles() ) {
                if ( eachFile.exists() ) {
                    if ( !eachFile.delete() ) {
                        errors.add( eachFile );
                    }
                }
            }
            if ( !errors.isEmpty() ) {
                throw new LifecycleException( "Unable to delete existing files: " + errors );
            }
        }

    }

}
