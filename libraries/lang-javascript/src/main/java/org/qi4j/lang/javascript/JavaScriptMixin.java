/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.lang.javascript;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.mozilla.javascript.*;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.scripting.ScriptException;
import org.qi4j.library.scripting.ScriptReloadable;

/**
 * Generic mixin that implements interfaces by delegating to JavaScript functions
 * using Rhino. Each method in an interface is declared as a JS function
 * in a file located in classpath with the name "<interface>.<method>.js",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.say.js
 */
@AppliesTo( JavaScriptMixin.AppliesTo.class )
public class JavaScriptMixin
    implements InvocationHandler, ScriptReloadable
{
    @This private Composite me;

    static private Scriptable standardScope;

    private HashMap<String, Function> cachedScripts;

    @Structure private TransientBuilderFactory factory;
    private Scriptable instanceScope;
    static
    {
        Context cx = Context.enter();
        standardScope = cx.initStandardObjects();
        Context.exit();
    }

    public JavaScriptMixin()
    {
        cachedScripts = new HashMap<String, Function>();
        Context cx = Context.enter();
        instanceScope = cx.newObject( standardScope );
        instanceScope.setPrototype( standardScope );
        Context.exit();

    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Context cx = Context.enter();
        try
        {
            Scriptable proxyScope = Context.toObject( proxy, instanceScope );
            proxyScope.setPrototype( instanceScope );
            proxyScope.put( "compositeBuilderFactory", proxyScope, factory );
            proxyScope.put( "This", proxyScope, me );
            Function fn = getFunction( cx, proxyScope, method );
            Object result = fn.call( cx, instanceScope, proxyScope, args );

            if( result instanceof Undefined )
            {
                return null;
            }
            else if( result instanceof Wrapper )
            {
                return ( (Wrapper) result ).unwrap();
            }
            else
            {
                return result;
            }
        }
        finally
        {
            Context.exit();
        }
    }

    @Override
    public void reloadScripts()
    {
        cachedScripts.clear();
    }

    private Function getFunction( Context cx, Scriptable scope, Method method )
        throws IOException
    {
        Class<?> declaringClass = method.getDeclaringClass();
        String classname = declaringClass.getName();
        String methodName = method.getName();
        String requestedFunctionName = classname + ":" + methodName;

        Function fx = cachedScripts.get( requestedFunctionName );
        if( fx != null )
        {
            return fx;
        }
        compileScripts( cx, scope, method );
        fx = cachedScripts.get( requestedFunctionName );
        return fx;
    }

    private void compileScripts( Context cx, Scriptable scope, Method method )
        throws IOException
    {
        URL scriptUrl = getFunctionResource( method );
        if( scriptUrl == null )
        {
            throw new IOException( "No script found for method " + method.getName() );
        }

        InputStream in = scriptUrl.openStream();
        BufferedReader scriptReader = new BufferedReader( new InputStreamReader( in ) );
        int lineNo = 1;
        String classname = method.getDeclaringClass().getName();
        while( true )
        {
            ScriptFragment fragment = extractFunction( scriptReader );
            if( "".equals( fragment.script.trim() ) )
            {
                break;
            }
            String functionName = parseFunctionName( fragment.script, scriptUrl.toString() );
            Function function = cx.compileFunction( scope, fragment.script, "<" + scriptUrl.toString() + ">", lineNo, null );
            cachedScripts.put( classname + ":" + functionName, function );
            lineNo = lineNo + fragment.numberOfLines;
        }
    }

    /**
     * Extracts the function name.
     * <p>
     * Since the fragment has been stripped of all comments, the first non-whitespace word to appear
     * should be "function" and the word after that should be the function name.
     * </p>
     *
     * @param script     The script snippet.
     * @param scriptName The name of the script being parsed.
     * @return the name of the function declared in this snippet.
     */
    private String parseFunctionName( String script, String scriptName )
    {
        // TODO optimize with hardcoded parser??
        StringTokenizer st = new StringTokenizer( script, " \t\n\r\f(){}", false );
        if( !st.hasMoreTokens() )
        {
            throw new ScriptException( "The word \"function\" was not found in script: " + scriptName );
        }
        String fx = st.nextToken();
        if( !"function".equals( fx ) )
        {
            throw new ScriptException( "The word \"function\" was not found in script: " + scriptName );
        }
        if( !st.hasMoreTokens() )
        {
            throw new ScriptException( "Invalid syntax in: " + scriptName + "\n No function name." );
        }
        return st.nextToken();
    }

    /**
     * Returns ONE function, minus comments.
     *
     * @param scriptReader The Reader of the script
     * @return A ScriptFragment containing the Script text for the function, and how many lines it is.
     * @throws IOException If a problem in the Reader occurs.
     */
    private ScriptFragment extractFunction( Reader scriptReader )
        throws IOException
    {
        ScriptFragment fragment = new ScriptFragment();
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;
        boolean lineComment = false;
        boolean blockComment = false;
        char lastCh = '\0';
        int braceCounter = 0;
        boolean notStarted = true;
        int b = scriptReader.read();
        int skip = 0;
        while( b != -1 && ( notStarted || braceCounter > 0 ) )
        {
            char ch = (char) b;
            if( !blockComment && !lineComment )
            {
                fragment.script = fragment.script + ch;
                if( !escaped )
                {
                    if( !inString && !inChar )
                    {
                        if( ch == '{' )
                        {
                            braceCounter++;
                            notStarted = false;
                        }
                        if( ch == '}' )
                        {
                            braceCounter--;
                        }
                    }
                    if( ch == '\"' )
                    {
                        inString = !inString;
                    }
                    if( ch == '\'' )
                    {
                        inChar = !inChar;
                    }
                    if( ch == '\\' )
                    {
                        escaped = true;
                    }
                    if( ch == '\n' )
                    {
                        fragment.numberOfLines++;
                    }
                    if( ch == '/' && lastCh == '/' )
                    {
                        lineComment = true;
                        fragment.script = fragment.script.substring( 0, fragment.script.length() - 2 );
                    }
                    if( ch == '*' && lastCh == '/' )
                    {
                        blockComment = true;
                        fragment.script = fragment.script.substring( 0, fragment.script.length() - 2 );
                    }
                }
                else
                {
                    if( ch == 'u' )
                    {
                        skip = 4;
                    }
                    else if( skip > 0 )
                    {
                        skip--;
                    }
                    else
                    {
                        escaped = false;
                    }
                }
            }
            else
            {
                if( lineComment )
                {
                    if( ch == '\n' )
                    {
                        lineComment = false;
                    }
                }
                if( blockComment )
                {
                    if( ch == '/' && lastCh == '*' )
                    {
                        blockComment = false;
                    }
                }
            }
            lastCh = ch;
            b = scriptReader.read();
        }
        return fragment;
    }

    private static URL getFunctionResource( Method method )
    {
        String scriptName = getScriptName( method );
        Class<?> declaringClass = method.getDeclaringClass();
        ClassLoader loader = declaringClass.getClassLoader();
        return loader.getResource( scriptName );
    }

    private static String getScriptName( Method method )
    {
        Class<?> declaringClass = method.getDeclaringClass();
        String classname = declaringClass.getName();
        return classname.replace( '.', '/' ) + ".js";
    }

    private static class ScriptFragment
    {
        String script = "";
        int numberOfLines = 0;
    }

    public static class AppliesTo
        implements AppliesToFilter
    {

        public boolean appliesTo( Method method, Class compositeType, Class mixin, Class modelClass )
        {
            return getFunctionResource( method ) != null;
        }

    }
}
