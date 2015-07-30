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

import java.lang.annotation.Retention;
import org.apache.zest.api.constraint.ConstraintDeclaration;
import org.apache.zest.api.constraint.Constraints;
import org.apache.zest.library.constraints.annotation.InstanceOf;
import org.apache.zest.library.constraints.annotation.NotEmpty;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ConstraintDeclaration
@Retention( RUNTIME )
@NotEmpty
@InstanceOf( String.class )
@Constraints( CronExpressionConstraint.class )
public @interface CronExpression
{
}
