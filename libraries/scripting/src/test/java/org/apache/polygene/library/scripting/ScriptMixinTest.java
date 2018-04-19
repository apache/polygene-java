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
package org.apache.polygene.library.scripting;

import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.structure.TypeLookup;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

public class ScriptMixinTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        LayerAssembly layer = module.layer();
        layer.application().setName( "Script Test" );
        module.transients( DomainType.class ).setMetaInfo( Scripting.JAVASCRIPT ).withMixins( ScriptMixin.class );
        module.transients( HelloSpeaker.class ).setMetaInfo( Scripting.GROOVY ).withMixins( ScriptMixin.class );
        module.values( HelloSpeaker.class ).setMetaInfo( Scripting.JAVASCRIPT ).withMixins( ScriptMixin.class );
    }

    @Test
    public void testInvoke() throws Throwable
    {
        DomainType domain1 = transientBuilderFactory.newTransient( DomainType.class );
        assertThat(domain1.do1("her message"), equalTo("[her message]") );
    }

    @Test
    public void testIsolation() throws Throwable
    {
        DomainType domain1 = transientBuilderFactory.newTransient( DomainType.class );
        DomainType domain2 = transientBuilderFactory.newTransient( DomainType.class );
        DomainType domain3 = transientBuilderFactory.newTransient( DomainType.class );
        assertThat(domain1.do1("her message"), equalTo("[her message]") );
        assertThat(domain2.do1("his message"), equalTo("[his message]") );
        assertThat(domain3.do1("its message"), equalTo("[its message]") );
        domain1.inc();
        domain1.inc();
        domain1.inc();
        domain1.inc();
        domain2.inc();
        domain2.inc();
        domain2.inc();
        domain3.inc();
        assertThat(domain1.count(), equalTo(4.0) );
        assertThat(domain2.count(), equalTo(3.0) );
        assertThat(domain3.count(), equalTo(1.0) );
    }

    @Test
    public void testBindings() {
        DomainType domain = transientBuilderFactory.newTransient( DomainType.class );

        Object _this = domain.whatIsThis();
        assertThat( _this, instanceOf(DomainType.class));
        assertThat( _this, instanceOf(TransientComposite.class ) );

        StateHolder state = domain.whatIsState( );
        assertThat( state.properties(), notNullValue());

        Application app  = domain.whatIsApplication( );
        assertThat( app.name(), equalTo("Script Test"));

        Layer layer = domain.whatIsLayer();
        assertThat( layer.name(), equalTo("Layer 1"));

        Module module = domain.whatIsModule();
        assertThat( module.name(), equalTo("Module 1"));

        ObjectFactory of = domain.whatIsObjectFactory( );
        assertThat( of, notNullValue());

        UnitOfWorkFactory uowf = domain.whatIsUnitOfWorkFactory( );
        assertThat( uowf, notNullValue());

        ValueBuilderFactory vbf = domain.whatIsValueBuilderFactory( );
        assertThat( vbf, notNullValue());

        TransientBuilderFactory tbf = domain.whatIsTransientBuilderFactory( );
        assertThat( tbf, notNullValue());

        ServiceFinder finder = domain.whatIsServiceFinder( );
        assertThat( finder, notNullValue());

        TypeLookup lookup = domain.whatIsTypeLookup( );
        assertThat( lookup, notNullValue());
    }


    @Test
    public void testJavascriptInvoke() throws Exception
    {
        HelloSpeaker speaker = valueBuilderFactory.newValue( HelloSpeaker.class );
        assertThat(speaker.sayHello(), equalTo("Hello, JavaScript"));
    }

    @Test
    public void testGroovyInvoke() throws Exception
    {
        HelloSpeaker speaker = transientBuilderFactory.newTransient( HelloSpeaker.class );
        assertThat(speaker.sayHello(), equalTo("Hello, Groovy"));
    }
}