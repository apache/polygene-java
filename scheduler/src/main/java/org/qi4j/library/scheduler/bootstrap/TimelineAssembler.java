/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.scheduler.bootstrap;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import org.qi4j.library.scheduler.timeline.TimelineRecordEntity;
import org.qi4j.library.scheduler.timeline.TimelineRecordValue;
import org.qi4j.library.scheduler.timeline.TimelineRecorderService;
import org.qi4j.library.scheduler.timeline.TimelineService;

/**
 * TODO Handle Visibility
 * 
 * @author Paul Merlin
 */
public class TimelineAssembler
        implements Assembler
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.addValues( TimelineRecordValue.class );

        module.addEntities( TimelineRecordEntity.class );

        module.addServices( TimelineService.class,
                            TimelineRecorderService.class );
    }

}
