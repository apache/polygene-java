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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.library.uowfile.internal.UoWFileFactory;

@Mixins( HasUoWFile.Mixin.class )
// START SNIPPET: contract
public interface HasUoWFile
{

    /**
     * IMPORTANT Use this {@link File} only inside read-only {@link UnitOfWork}s
     */
    File attachedFile();

    File managedFile();
    // END SNIPPET: contract

    class Mixin
            implements HasUoWFile
    {

        @Service
        private UoWFileFactory uowFileFactory;

        @This
        private UoWFileLocator locator;

        @Override
        public File attachedFile()
        {
            return locator.locateAttachedFile();
        }

        @Override
        public File managedFile()
        {
            return uowFileFactory.createCurrentUoWFile( locator.locateAttachedFile() ).asFile();
        }

    }
    // START SNIPPET: contract
}
// END SNIPPET: contract
