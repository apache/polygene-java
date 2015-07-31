/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.metrics.yammer;

import com.yammer.metrics.core.Histogram;
import org.qi4j.api.metrics.MetricsHistogram;

public class YammerHistogram
    implements MetricsHistogram
{
    private Histogram histogram;

    public YammerHistogram( Histogram histogram )
    {
        this.histogram = histogram;
    }

    @Override
    public void update( long newValue )
    {
        histogram.update( newValue );
    }
}
