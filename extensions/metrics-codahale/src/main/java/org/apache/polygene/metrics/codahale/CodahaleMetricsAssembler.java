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

package org.apache.polygene.metrics.codahale;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Slf4jReporter;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.ServiceDeclaration;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CodahaleMetricsAssembler
    extends Assemblers.VisibilityIdentity<CodahaleMetricsAssembler>
{
    private final CodahaleMetricsDeclaration declaration = new CodahaleMetricsDeclaration();

    public CodahaleMetricsAssembler withPrefix( String prefix )
    {
        declaration.prefix = prefix;
        return this;
    }

    public CodahaleMetricsAssembler withFullyQualifiedClassNames()
    {
        declaration.fqcn = true;
        return this;
    }

    public CodahaleMetricsAssembler withSimpleClassNames()
    {
        declaration.fqcn = false;
        return this;
    }

    public CodahaleMetricsAssembler withJmx()
    {
        declaration.jmx = true;
        return this;
    }

    public CodahaleMetricsAssembler withoutJmx()
    {
        declaration.jmx = false;
        return this;
    }

    public CodahaleMetricsAssembler withConsoleReporter( PrintStream out, long period, TimeUnit timeunit )
    {
        declaration.reportersFactories.add( metricRegistry -> {
            ConsoleReporter reporter = ConsoleReporter.forRegistry( metricRegistry ).outputTo( out ).build();
            reporter.start( period, timeunit );
            return reporter;
        });
        return this;
    }

    public CodahaleMetricsAssembler withSlf4jReporter( Slf4jReporter.LoggingLevel level, long period, TimeUnit timeunit )
    {
        declaration.reportersFactories.add( metricRegistry -> {
            Slf4jReporter reporter = Slf4jReporter.forRegistry( metricRegistry ).withLoggingLevel( level ).build();
            reporter.start( period, timeunit );
            return reporter;
        });
        return this;
    }

    public CodahaleMetricsAssembler withCsvReporter( File outDirectory, long period, TimeUnit timeunit )
    {
        declaration.reportersFactories.add( metricRegistry -> {
            CsvReporter reporter = CsvReporter.forRegistry( metricRegistry ).build( outDirectory );
            reporter.start( period, timeunit );
            return reporter;
        });
        return this;
    }

    public CodahaleMetricsAssembler withReporter( Function<MetricRegistry, Reporter> factory )
    {
        declaration.reportersFactories.add( factory );
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        ServiceDeclaration service = module.services( CodahaleMetricsProvider.class )
                .setMetaInfo( declaration )
                .instantiateOnStartup()
                .visibleIn( visibility() );
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
    }
}
