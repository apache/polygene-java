/*
 * Copyright (c) 2010-2012, Paul Merlin. All Rights Reserved.
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.scheduler.schedule.cron;

import org.codeartisans.sked.crontab.schedule.CronSchedule;
import org.qi4j.api.constraint.Constraint;

public class CronExpressionConstraint
        implements Constraint<CronExpression, String>
{

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid( CronExpression annotation, String cronExpression )
    {
        return CronSchedule.isExpressionValid( cronExpression );
    }

}
