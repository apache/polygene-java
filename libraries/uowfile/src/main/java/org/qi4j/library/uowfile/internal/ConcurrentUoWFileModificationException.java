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
package org.qi4j.library.uowfile.internal;

import java.util.Collections;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;

public class ConcurrentUoWFileModificationException
        extends ConcurrentEntityModificationException
{

    private final Iterable<UoWFile> concurrentlyModifiedFiles;

    ConcurrentUoWFileModificationException( Iterable<UoWFile> concurrentlyModifiedFiles )
    {
        super( Collections.<EntityComposite>emptyList() );
        this.concurrentlyModifiedFiles = concurrentlyModifiedFiles;
    }

    public Iterable<UoWFile> concurrentlyModifiedUoWFiles()
    {
        return concurrentlyModifiedFiles;
    }

    @Override
    public String getMessage()
    {
        return "Files changed concurently: " + concurrentlyModifiedFiles;
    }

}
