/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.spring.bootstrap;

import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.springframework.context.ApplicationContextAware;

/**
 * Run a Zest Application as a Spring Bean and export its Services to Spring.
 * <p>
 * Steps to export Zest service:
 * </p>
 * <ul>
 * <li>Create spring BeanFactory service of Apache Zest services to export.</li>
 * <li>Create a class that extends {@link ZestApplicationBootstrap}.</li>
 * <li>Sets the layer and module that register BeanFactory service.</li>
 * <li>Assemble Zest application by implementing #assemble method.</li>
 * <li>Sets the identity of bean factory service. This identity is the spring
 * bean name.</li>
 * <li>Declare Zest bootstrap in spring xml application context.
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *
 * &lt;beans xmlns="http://www.springframework.org/schema/beans"
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * xmlns:zest="http://zest.apache.org/schema/zest/spring"
 * xsi:schemaLocation="
 * http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
 * http://zest.apache.org/schema/zest/spring http://zest.apache.org/schema/zest/spring/spring-0.5.xsd"&gt;
 *
 * &lt;!-- class that implements ZestApplicationBootstrap --&gt;
 *
 * &lt;zest:bootstrap class="org.apache.zest.library.spring.bootstrap.ZestTestBootstrap"/&gt;
 *
 * &lt;bean id="commentServiceHolder" class="org.apache.zest.library.spring.bootstrap.CommentServiceHolder"&gt;
 *
 * &lt;constructor-arg ref="commentService"/&gt; &lt;!-- Reference Zest comment service --&gt;
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
 * Look at org.apache.zest.library.spring.bootstrap.ZestExportServiceTest for sample
 * implementation.
 * </p>
 */
public abstract class ZestApplicationBootstrap
{
    /**
     * Assembles Zest application.
     * 
     * @param applicationAssembly
     *            Zest application assembly. Must not be {@code null}.
     * @throws AssemblyException
     *             Thrown if assemblies fails.
     */
    public abstract void assemble( ApplicationAssembly applicationAssembly ) throws AssemblyException;
}
