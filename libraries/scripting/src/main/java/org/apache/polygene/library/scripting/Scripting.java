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

import javax.script.ScriptEngineManager;

// START SNIPPET: supported
public class Scripting
{
    public static final Scripting ECMASCRIPT = new Scripting( "nashorn", ".js" );
    public static final Scripting GROOVY = new Scripting( "groovy", ".groovy" );
    public static final Scripting JAVASCRIPT = new Scripting( "nashorn", ".js" );
    public static final Scripting KOTLIN = new Scripting( "kotlin", ".kts" );
    public static final Scripting LUA = new Scripting( "lua", ".lua" );
    public static final Scripting PYTHON = new Scripting( "python", ".py" );
    public static final Scripting RUBY = new Scripting( "jruby", ".rb" );
    // END SNIPPET: supported

    private String scriptEngine;
    private String extension;

    public Scripting( String extension )
    {
        this.extension = extension;
    }

    public Scripting( String scriptEngine, String extension )
    {
        this.scriptEngine = scriptEngine;
        this.extension = extension;
    }

    public String engine()
    {
        return scriptEngine;
    }

    public String extension()
    {
        return extension;
    }

    @Override
    public String toString()
    {
        if( scriptEngine == null )
        {
            return "for script extension '" + extension + "'";
        }
        return "'" + scriptEngine + "'";
    }

    public static void main( String[] args )
    {
        new ScriptEngineManager().getEngineFactories().forEach( factory -> System.out.println(factory));
    }
}
