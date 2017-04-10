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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.injection.scope.State;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Layer;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.spi.PolygeneSPI;

public class ScriptMixin
    implements InvocationHandler, ScriptReloadable, ScriptRedirect, ScriptAttributes, ScriptAttributes.All
{
    private final CompositeDescriptor descriptor;
    private ScriptEngine engine;

    public ScriptMixin( @Structure PolygeneSPI spi,
                        @This Object thisComposite,
                        @State StateHolder state,
                        @Structure Layer layer,
                        @Structure Module module,
                        @Structure Application application )
    {
        descriptor = spi.compositeDescriptorFor( thisComposite );
        engine = createNewEngine();
        Bindings mixinBindings = engine.getBindings( ScriptContext.ENGINE_SCOPE );
        mixinBindings.put( "Polygene", spi );
        mixinBindings.put( "application", application );
        mixinBindings.put( "layer", layer );
        mixinBindings.put( "module", module );
        mixinBindings.put( "This", thisComposite );
        mixinBindings.put( "state", state );
        mixinBindings.put( "objectFactory", module.objectFactory() );
        mixinBindings.put( "unitOfWorkFactory", module.unitOfWorkFactory() );
        mixinBindings.put( "valueBuilderFactory", module.valueBuilderFactory() );
        mixinBindings.put( "transientBuilderFactory", module.transientBuilderFactory() );
        mixinBindings.put( "serviceFinder", module.serviceFinder() );
        mixinBindings.put( "typeLookup", module.typeLookup() );
    }

    private ScriptEngine createNewEngine()
    {
        Scripting scripting = descriptor.metaInfo( Scripting.class );
        ScriptEngine engine = getScriptEngine( scripting );
        String scriptName = descriptor.primaryType().getName().replaceAll( "\\.", "/" ) + scripting.extension();
        try( BufferedReader reader = new BufferedReader( new InputStreamReader( getClass().getClassLoader().getResourceAsStream( scriptName ) ) ) )
        {
            engine.eval( reader );
        }
        catch( IOException e )
        {
            throw new ScriptException( "Unable to load " + scriptName + " for " + descriptor, e );
        }
        catch( javax.script.ScriptException e )
        {
            throw new ScriptException( "Unable to parse " + scriptName + ".", e );
        }
        return engine;
    }

    private ScriptEngine getScriptEngine( Scripting scripting )
    {
        ScriptEngineManager manager = new ScriptEngineManager( getClass().getClassLoader() );
        String engineName = scripting.engine();
        ScriptEngine engine;
        if( engineName != null )
        {
            engine = manager.getEngineByName( engineName );
        }
        else
        {
            engine = manager.getEngineByExtension( scripting.extension() );
        }
        if( engine == null )
        {
            throw new ScriptException( "Engine " + scripting + " is not available. Perhaps missing implementation on the classpath." );
        }
        return engine;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] objects )
        throws Throwable
    {
        Object result = ( (Invocable) engine ).invokeFunction( method.getName(), objects );
        return castInvocationResult( method.getReturnType(), result );
    }

    @Override
    public void reloadScript()
    {
        engine = createNewEngine();
    }

    @Override
    public void setStdOut( Writer writer )
    {
        writer = wrap( writer );
        engine.getContext().setWriter( writer );
    }

    @Override
    public void setStdErr( Writer writer )
    {
        engine.getContext().setErrorWriter( writer );
    }

    @Override
    public void setStdIn( Reader reader )
    {
        if( !( reader instanceof BufferedReader ) )
        {
            reader = new BufferedReader( reader );
        }
        engine.getContext().setReader( reader );
    }

    private Writer wrap( Writer writer )
    {
        if( writer instanceof BufferedWriter )
        {
            return writer;
        }
        return new BufferedWriter( writer );
    }

    @Override
    public Object getAttribute( String name )
    {
        return engine.getContext().getAttribute( name );
    }

    @Override
    public Object getEngineAttribute( String name )
    {
        return engine.getContext().getAttribute( name, ScriptContext.ENGINE_SCOPE );
    }

    @Override
    public Object getGlobalAttribute( String name )
    {
        return engine.getContext().getAttribute( name, ScriptContext.GLOBAL_SCOPE );
    }

    @Override
    public void setEngineAttribute( String name, Object value )
    {
        engine.getContext().setAttribute( name, value, ScriptContext.ENGINE_SCOPE );
    }

    @Override
    public void setGlobalAttribute( String name, Object value )
    {
        engine.getContext().setAttribute( name, value, ScriptContext.GLOBAL_SCOPE );
    }

    /**
     * Needed to prevent class cast exception between boxed and unboxed types.
     * Explicit casting to primitive type, triggers the auto-unboxing compiler trick.
     */
    @SuppressWarnings( "RedundantCast" )
    private static Object castInvocationResult( Class<?> returnType, Object result )
    {
        if( void.class.equals( returnType ) || Void.class.equals( returnType ) )
        {
            return null;
        }
        if( returnType.isPrimitive() )
        {
            if( char.class.equals( returnType ) )
            {
                return (char) result;
            }
            if( boolean.class.equals( returnType ) )
            {
                return (boolean) result;
            }
            if( short.class.equals( returnType ) )
            {
                return (short) result;
            }
            if( int.class.equals( returnType ) )
            {
                return (int) result;
            }
            if( byte.class.equals( returnType ) )
            {
                return (byte) result;
            }
            if( long.class.equals( returnType ) )
            {
                return (long) result;
            }
            if( float.class.equals( returnType ) )
            {
                return (float) result;
            }
            if( double.class.equals( returnType ) )
            {
                return (double) result;
            }
        }
        return returnType.cast( result );
    }
}
