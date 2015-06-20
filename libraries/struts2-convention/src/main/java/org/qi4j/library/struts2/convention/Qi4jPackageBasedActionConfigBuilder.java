/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.library.struts2.convention;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import java.lang.reflect.Modifier;
import org.apache.struts2.convention.PackageBasedActionConfigBuilder;
import org.qi4j.library.struts2.ActionConfiguration;

public class Qi4jPackageBasedActionConfigBuilder
    extends PackageBasedActionConfigBuilder
{

    private final ActionConfiguration actionConfiguration;

    @Inject
    public Qi4jPackageBasedActionConfigBuilder( Configuration configuration, Container container,
                                                ObjectFactory objectFactory,
                                                @Inject( "struts.convention.redirect.to.slash" ) String redirectToSlash,
                                                @Inject( "struts.convention.default.parent.package" ) String defaultParentPackage,
                                                ActionConfiguration actionConfiguration
    )
    {
        super( configuration, container, objectFactory, redirectToSlash, defaultParentPackage );
        this.actionConfiguration = actionConfiguration;
    }

    @Override
    public void buildActionConfigs()
    {
        buildConfiguration( actionConfiguration.getClasses() );
    }

    @Override
    protected boolean cannotInstantiate( Class<?> actionClass )
    {
        return actionClass.isAnnotation()
               || actionClass.isEnum()
               || ( !actionClass.isInterface() && ( actionClass.getModifiers() & Modifier.ABSTRACT ) != 0 );
    }
}
