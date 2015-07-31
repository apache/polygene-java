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

import com.yammer.metrics.Metrics;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.reporting.CsvReporter;
import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class YammerMetricsAssembler
    implements Assembler
{

    private AbstractPollingReporter reporter;

    /**
     * Default constructor only creates a Yammer JMXReporter
     */
    public YammerMetricsAssembler()
    {
    }

    /**
     * Creates a ConsoleReporter and sends the output to the given PrintStream.
     *
     * @param out      The PrintStream to receive the output.
     * @param period   The reporting interval.
     * @param timeunit The TimeUnit for the reporting interval.
     */
    public YammerMetricsAssembler( PrintStream out, long period, TimeUnit timeunit )
    {
        reporter = new ConsoleReporter( out );
        try
        {
            reporter.start( period, timeunit );
        }
        catch( RuntimeException e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Creates a CSV reporter and writes the result to the given directory
     *
     * @param outDirectory The directory to write the result to.
     * @param period       The reporting interval.
     * @param timeunit     The TimeUnit for the reporting interval.
     */
    public YammerMetricsAssembler( File outDirectory, long period, TimeUnit timeunit )
    {
        reporter = new CsvReporter( Metrics.defaultRegistry(), outDirectory );
        reporter.start( period, timeunit );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( YammerMetricsProvider.class )
            .instantiateOnStartup()
            .visibleIn( Visibility.application );
    }

    /**
     * Closing any Reporter that has been started.
     */
    public void shutdown()
    {
        reporter.shutdown();
    }
}
