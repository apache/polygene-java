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
 */
package org.apache.polygene.metrics.codahale;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.metrics.Metric;
import org.apache.polygene.api.metrics.MetricsCounter;
import org.apache.polygene.api.metrics.MetricsCounterFactory;
import org.apache.polygene.api.metrics.MetricsGauge;
import org.apache.polygene.api.metrics.MetricsGaugeFactory;
import org.apache.polygene.api.metrics.MetricsHealthCheck;
import org.apache.polygene.api.metrics.MetricsHealthCheckFactory;
import org.apache.polygene.api.metrics.MetricsHistogram;
import org.apache.polygene.api.metrics.MetricsHistogramFactory;
import org.apache.polygene.api.metrics.MetricsMeter;
import org.apache.polygene.api.metrics.MetricsMeterFactory;
import org.apache.polygene.api.metrics.MetricsTimer;
import org.apache.polygene.api.metrics.MetricsTimerFactory;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.metrics.codahale.assembly.CodahaleMetricsDeclaration;
import org.apache.polygene.spi.metrics.MetricsProviderAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;

public class CodahaleMetricsMixin extends MetricsProviderAdapter
    implements CodahaleMetricsProvider
{
    @Structure
    private Application app;

    @Uses
    private ServiceDescriptor descriptor;

    private final List<Reporter> reporters = new ArrayList<>();

    private MetricRegistry metricRegistry;
    private HealthCheckRegistry healthCheckRegistry;
    private String prefix;
    private boolean fqcn;

    @Override
    public void activateService() {
        metricRegistry = new MetricRegistry();
        healthCheckRegistry = new HealthCheckRegistry();
        CodahaleMetricsDeclaration declaration = descriptor.metaInfo( CodahaleMetricsDeclaration.class );
        prefix = declaration.prefix() != null ? declaration.prefix() : app.name();
        fqcn = declaration.fqcn();
        if( declaration.jmx() )
        {
            JmxReporter jmxReporter = JmxReporter.forRegistry( metricRegistry ).build();
            jmxReporter.start();
            reporters.add( jmxReporter );
        }
        for( Function<MetricRegistry, Reporter> reporterFactory : declaration.reportersFactories())
        {
            reporters.add( reporterFactory.apply( metricRegistry ) );
        }
    }

    @Override
    public void passivateService() throws PassivationException {
        List<Exception> errors = new ArrayList<>();
        for( Reporter reporter : reporters )
        {
            if( Closeable.class.isAssignableFrom( reporter.getClass() ) )
            try
            {
                ( (Closeable) reporter ).close();
            }
            catch ( IOException ex )
            {
                errors.add( ex );
            }
        }
        reporters.clear();
        try
        {
            metricRegistry.removeMatching( MetricFilter.ALL );
        }
        catch( Exception ex )
        {
            errors.add( ex );
        }
        metricRegistry = null;
        for( String healthCheckName : healthCheckRegistry.getNames() )
        {
            healthCheckRegistry.unregister( healthCheckName );
        }
        healthCheckRegistry = null;
        prefix = null;
        if( !errors.isEmpty() )
        {
            throw new PassivationException( errors );
        }
    }

    @Override
    public MetricRegistry metricRegistry()
    {
        return metricRegistry;
    }

    @Override
    public HealthCheckRegistry healthCheckRegistry()
    {
        return healthCheckRegistry;
    }

    @Override
    protected MetricsTimerFactory createMetricsTimerFactory()
    {
        return new MetricsTimerFactory()
        {
            @Override
            public MetricsTimer createTimer( String name )
            {
                return new CodahaleTimer( metricRegistry.timer( name( prefix, name ) ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return metricRegistry.getTimers().values().stream().map( CodahaleTimer::new );
            }
        };
    }

    @Override
    protected MetricsMeterFactory createMetricsMeterFactory()
    {
        return new MetricsMeterFactory()
        {
            @Override
            public MetricsMeter createMeter( String name )
            {
                return new CodahaleMeter( metricRegistry.meter( name( prefix, name ) ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return metricRegistry.getMeters().values().stream().map( CodahaleMeter::new );
            }
        };
    }

    @Override
    protected MetricsHistogramFactory createMetricsHistogramFactory()
    {
        return new MetricsHistogramFactory()
        {
            @Override
            public MetricsHistogram createHistogram( String name )
            {
                return new CodahaleHistogram( metricRegistry.histogram( name( prefix, name ) ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return metricRegistry.getHistograms().values().stream().map( CodahaleHistogram::new );
            }
        };
    }

    @Override
    protected MetricsHealthCheckFactory createMetricsHealthCheckFactory()
    {
        return new MetricsHealthCheckFactory()
        {
            @Override
            public MetricsHealthCheck registerHealthCheck( String name, MetricsHealthCheck check )
            {
                HealthCheck codahaleCheck = new HealthCheck() {
                    @Override
                    protected Result check() throws Exception {
                        return CodahaleHealthCheck.unwrap( check.check() );
                    }
                };
                healthCheckRegistry.register( name( prefix, name ), codahaleCheck );
                return new CodahaleHealthCheck( check );
            }

            @Override
            public Stream<Metric> registered()
            {
                return healthCheckRegistry.getNames().stream().map( name -> new MetricsHealthCheck() {
                    @Override
                    public Result check() throws Exception {
                        return CodahaleHealthCheck.wrap( healthCheckRegistry.runHealthCheck( name ) );
                    }
                });
            }
        };
    }

    @Override
    protected MetricsGaugeFactory createMetricsGaugeFactory()
    {
        return new MetricsGaugeFactory()
        {
            @Override
            public <T> MetricsGauge<T> registerGauge( String name, final MetricsGauge<T> gauge )
            {
                Gauge<T> codahaleGauge = gauge::value;
                metricRegistry.register( name( prefix, name ), codahaleGauge );
                return new CodahaleGauge<>( codahaleGauge );
            }

            @Override
            @SuppressWarnings( "unchecked" )
            public Stream<Metric> registered()
            {
                return metricRegistry.getGauges().values().stream().map( CodahaleGauge::new );
            }
        };
    }

    @Override
    protected MetricsCounterFactory createMetricsCounterFactory()
    {
        return new MetricsCounterFactory()
        {
            @Override
            public MetricsCounter createCounter( String name )
            {
                return new CodahaleCounter( metricRegistry.counter( name( prefix, name) ) );
            }

            @Override
            public Stream<Metric> registered()
            {
                return metricRegistry.getCounters().values().stream().map( CodahaleCounter::new );
            }
        };
    }

}
