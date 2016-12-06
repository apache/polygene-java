/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.uowfile.singular;

import java.io.File;
import java.io.IOException;
import org.apache.zest.api.entity.Lifecycle;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;

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
        {
            // NOOP
        }

        @Override
        public void remove()
            throws IOException
        {
            // We use the managed file so that if the UoW gets discarded the file will be restored
            File file = hasUoWFile.managedFile();
            if( file.exists() && !file.delete() )
            {
                throw new IOException( "Unable to delete existing file: " + file );
            }
        }
    }

}
