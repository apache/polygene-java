/*
 * Copyright (c) 2011, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.alarm;

/** AlarmClass is required attribute for Alarms, to indicate the urgency of the AlarmPoint.
 *
 */
public enum AlarmClass
{
    /**
     * A-alarms requires immediate attention, no matter time of day or day of year. In the enterprise world, they
     * indicate issue with mission-critical, 24/7, functions.
     */
    A,

    /** B-alarms require attention during working hours.
     *
     */
    B,

    /** C-alarm indicate things that needs to be attended to during regular maintenance cycles or when spare time is
     * available.
     */
    C,

    /** D-alarms are used for notifications that indicates smaller issues, or things that can not be dealt with
     * by humans and will self-correct over time. They are also used to produce events for general notifications
     * that are informative in nature, such as reports.
     */
    D
}
