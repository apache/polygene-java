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
package org.apache.polygene.test.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import pl.domzal.junit.docker.rule.DockerRuleBuilder;
import pl.domzal.junit.docker.rule.WaitFor;
import pl.domzal.junit.docker.rule.wait.LineListener;
import pl.domzal.junit.docker.rule.wait.StartCondition;
import pl.domzal.junit.docker.rule.wait.StartConditionCheck;

import static java.util.stream.Collectors.joining;
import static org.junit.Assume.assumeFalse;

public class DockerRule
    implements TestRule
{
    private final boolean dockerDisabled = Boolean.valueOf( System.getProperty( "DOCKER_DISABLED", "false" ) );
    private final pl.domzal.junit.docker.rule.DockerRule dockerRule;

    public DockerRule( String image, int... portsToWaitFor )
    {
        this( image, null, WaitFor.tcpPort( portsToWaitFor ) );
    }

    public DockerRule( String image, String... logMessageSequenceToWaitFor )
    {
        this( image, null, WaitFor.logMessageSequence( logMessageSequenceToWaitFor ) );
    }

    public DockerRule( String image, Map<String, String> environment, String... logMessageSequnceToWaitFor )
    {
        this( image, environment, WaitFor.logMessageSequence( logMessageSequnceToWaitFor ) );
    }

    public DockerRule( String image, Long delay, int... portsToWaitFor )
    {
        this( image, null, new DelayChecker( delay ), WaitFor.tcpPort( portsToWaitFor ), new DelayChecker( delay ) );
    }

    public DockerRule( String image, Long delay, String... logMessageSequenceToWaitFor )
    {
        this( image, null, WaitFor.logMessageSequence( logMessageSequenceToWaitFor ), new DelayChecker( delay ) );
    }

    public DockerRule( String image, Map<String, String> environment, Long delay, String... logMessageSequnceToWaitFor )
    {
        this( image, environment, WaitFor.logMessageSequence( logMessageSequnceToWaitFor ), new DelayChecker( delay ) );
    }

    public DockerRule( String image, Map<String, String> environment, StartCondition... waitFor )
    {
        if( environment == null )
        {
            environment = Collections.emptyMap();
        }
        if( dockerDisabled )
        {
            dockerRule = null;
        }
        else
        {
            DockerRuleBuilder builder = pl.domzal.junit.docker.rule.DockerRule
                .builder()
                .imageName( "org.apache.polygene:org.apache.polygene.internal.docker-" + image )
                .publishAllPorts( true )
                .waitForTimeout( 60000 )
                .waitFor( rule -> new AndChecker( rule, waitFor ) );
            environment.entrySet().forEach( entry -> builder.env( entry.getKey(), entry.getValue() ) );
            dockerRule = builder.build();
        }
    }

    @Override
    public Statement apply( Statement base, Description description )
    {
        assumeFalse( dockerDisabled );
        return dockerRule.apply( base, description );
    }

    public String getDockerHost()
    {
        return dockerRule.getDockerHost();
    }

    public int getExposedContainerPort( String containerPort )
    {
        return Integer.valueOf( dockerRule.getExposedContainerPort( containerPort ) );
    }

    public class AndChecker
        implements StartConditionCheck, LineListener
    {
        private List<StartConditionCheck> allOf;

        public AndChecker( pl.domzal.junit.docker.rule.DockerRule rule, StartCondition... allOf )
        {
            this.allOf = Arrays.stream( allOf ).map( cond -> cond.build( rule ) ).collect( Collectors.toList() );
        }

        @Override
        public boolean check()
        {
            return allOf.stream()
                        .allMatch( StartConditionCheck::check );
        }

        @Override
        public String describe()
        {
            return allOf.stream()
                        .map( StartConditionCheck::describe )
                        .collect( joining( ",", "and(", ")" ) );
        }

        @Override
        public void after()
        {
            allOf.forEach( StartConditionCheck::after );
        }

        @Override
        public void nextLine( String line )
        {
            allOf.forEach( listener ->
                           {
                               if( listener instanceof LineListener )
                               {
                                   ( (LineListener) listener ).nextLine( line );
                               }
                           } );
        }
    }
}
