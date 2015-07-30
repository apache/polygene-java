/*
 * Copyright (c) 2010-2012, Paul Merlin.
 * Copyright (c) 2012, Niclas Hedhman.
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
package org.apache.zest.library.scheduler.schedule.cron;

import org.codeartisans.sked.cron.CronSchedule;
import org.apache.zest.api.constraint.Constraint;

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
