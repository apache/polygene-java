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
package org.apache.polygene.library.spring.bootstrap;

import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.springframework.context.ApplicationContextAware;

/**
 * Run a Polygene Application as a Spring Bean and export its Services to Spring.
 * <p>
 * Steps to export Polygene service:
 * </p>
 * <ul>
 * <li>Create spring BeanFactory service of Apache Polygene services to export.</li>
 * <li>Create a class that extends {@link PolygeneApplicationBootstrap}.</li>
 * <li>Sets the layer and module that register BeanFactory service.</li>
 * <li>Assemble Polygene application by implementing #assemble method.</li>
 * <li>Sets the reference of bean factory service. This reference is the spring
 * bean name.</li>
 * <li>Declare Polygene bootstrap in spring xml application context.
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *
 * &lt;beans xmlns="http://www.springframework.org/schema/beans"
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * xmlns:polygene="http://polygene.apache.org/schema/polygene/spring"
 * xsi:schemaLocation="
 * http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
 * http://polygene.apache.org/schema/polygene/spring http://polygene.apache.org/schema/polygene/spring/spring-0.5.xsd"&gt;
 *
 * &lt;!-- class that implements PolygeneApplicationBootstrap --&gt;
 *
 * &lt;polygene:bootstrap class="org.apache.polygene.library.spring.bootstrap.PolygeneTestBootstrap"/&gt;
 *
 * &lt;bean id="commentServiceHolder" class="org.apache.polygene.library.spring.bootstrap.CommentServiceHolder"&gt;
 *
 * &lt;constructor-arg ref="commentService"/&gt; &lt;!-- Reference Polygene comment service --&gt;
 *
 * &lt;/bean&gt;
 * </code></pre>
 * </li>
 * </ul>
 * <p>
 * <b>Importing Spring beans as services</b><br>
 * </p>
 * <ol>
 * <li>Application bootstrap class must implement interface
 * {@link ApplicationContextAware}.</li>
 * <li>In the application bootstrap import service to the module using method
 * {@link ModuleAssembly#importedServices(Class...)}.</li>
 * <li>Set concrete Spring bean as meta-data of the imported service.</li>
 * </ol>
 * <p>
 * Look at org.apache.polygene.library.spring.bootstrap.PolygeneExportServiceTest for sample
 * implementation.
 * </p>
 */
public abstract class PolygeneApplicationBootstrap
{
    /**
     * Assembles Polygene application.
     * 
     * @param applicationAssembly
     *            Polygene application assembly. Must not be {@code null}.
     * @throws AssemblyException
     *             Thrown if assemblies fails.
     */
    public abstract void assemble( ApplicationAssembly applicationAssembly ) throws AssemblyException;
}
