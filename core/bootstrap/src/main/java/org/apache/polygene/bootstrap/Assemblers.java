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
package org.apache.polygene.bootstrap;

/**
 * Assembler adapters for common use cases (visibility, reference, configuration).
 */
public class Assemblers
{
    private Assemblers()
    {
    }

    /**
     * Assembler with Visibility interface.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public interface Visible<AssemblerType>
        extends Assembler
    {
        /**
         * Set Visibility.
         * @param visibility Visibility
         * @return This Assembler instance
         */
        AssemblerType visibleIn( org.apache.polygene.api.common.Visibility visibility );

        /**
         * Get Visibility.
         * <p>Default to {@link org.apache.polygene.api.common.Visibility#module}.</p>
         * @return Visibility
         */
        org.apache.polygene.api.common.Visibility visibility();
    }

    /**
     * Assembler with Identity interface.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public interface Identifiable<AssemblerType>
        extends Assembler
    {
        /**
         * Set Identity.
         * @param identity Identity
         * @return This Assembler instance
         */
        AssemblerType identifiedBy( String identity );

        /**
         * @return {@literal true} if {@link #identity()} do not return null, {@literal false} otherwise
         */
        boolean hasIdentity();

        /**
         * Get Identity.
         * <p>Default to {@literal null}.</p>
         * @return Identity
         */
        String identity();
    }

    /**
     * Assembler with Configuration interface.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public interface Configurable<AssemblerType>
        extends Assembler
    {
        /**
         * Set Configuration Module and Visibility.
         * @param configModule Configuration Module
         * @param configVisibility Configuration Visiblity
         * @return This Assembler instance
         */
        AssemblerType withConfig( ModuleAssembly configModule,
                                  org.apache.polygene.api.common.Visibility configVisibility );

        /**
         * @return {@literal true} if {@link #configModule() ()} do not return null, {@literal false} otherwise
         */
        boolean hasConfig();

        /**
         * Get Configuration Module.
         * <p>Default to {@literal null}.</p>
         * @return Configuration Module
         */
        ModuleAssembly configModule();

        /**
         * Get Configuration Visibility.
         * <p>Default to {@link org.apache.polygene.api.common.Visibility#module}.</p>
         * @return Configuration Visibility
         */
        org.apache.polygene.api.common.Visibility configVisibility();
    }

    /**
     * Assembler with Visibility adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class Visibility<AssemblerType> extends AssembleChecker
        implements Visible<AssemblerType>
    {
        private org.apache.polygene.api.common.Visibility visibility = org.apache.polygene.api.common.Visibility.module;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType visibleIn( org.apache.polygene.api.common.Visibility visibility )
        {
            this.visibility = visibility;
            return (AssemblerType) this;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility visibility()
        {
            return visibility;
        }
    }

    /**
     * Assembler with Identity adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class Identity<AssemblerType> extends AssembleChecker
        implements Identifiable<AssemblerType>
    {
        private String identity;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType identifiedBy( String identity )
        {
            this.identity = identity;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasIdentity()
        {
            return identity != null;
        }

        @Override
        public final String identity()
        {
            return identity;
        }
    }

    /**
     * Assembler with Configuration adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class Config<AssemblerType> extends AssembleChecker
        implements Configurable<AssemblerType>
    {
        private ModuleAssembly configModule = null;
        private org.apache.polygene.api.common.Visibility configVisibility = org.apache.polygene.api.common.Visibility.module;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType withConfig( ModuleAssembly configModule,
                                               org.apache.polygene.api.common.Visibility configVisibility )
        {
            this.configModule = configModule;
            this.configVisibility = configVisibility;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasConfig()
        {
            return configModule != null;
        }

        @Override
        public final ModuleAssembly configModule()
        {
            return configModule;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility configVisibility()
        {
            return configVisibility;
        }
    }

    /**
     * Assembler with Visibility and Identity adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class VisibilityIdentity<AssemblerType> extends AssembleChecker
        implements Visible<AssemblerType>,
                   Identifiable<AssemblerType>
    {
        private org.apache.polygene.api.common.Visibility visibility = org.apache.polygene.api.common.Visibility.module;
        private String identity;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType visibleIn( org.apache.polygene.api.common.Visibility visibility )
        {
            this.visibility = visibility;
            return (AssemblerType) this;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility visibility()
        {
            return visibility;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType identifiedBy( String identityString )
        {
            this.identity = identityString;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasIdentity()
        {
            return identity != null;
        }

        @Override
        public final String identity()
        {
            return identity;
        }
    }

    /**
     * Assembler with Visibility and Configuration adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class VisibilityConfig<AssemblerType> extends AssembleChecker
        implements Visible<AssemblerType>,
                   Configurable<AssemblerType>
    {
        private org.apache.polygene.api.common.Visibility visibility = org.apache.polygene.api.common.Visibility.module;
        private ModuleAssembly configModule = null;
        private org.apache.polygene.api.common.Visibility configVisibility = org.apache.polygene.api.common.Visibility.module;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType visibleIn( org.apache.polygene.api.common.Visibility visibility )
        {
            this.visibility = visibility;
            return (AssemblerType) this;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility visibility()
        {
            return visibility;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType withConfig( ModuleAssembly configModule,
                                               org.apache.polygene.api.common.Visibility configVisibility )
        {
            this.configModule = configModule;
            this.configVisibility = configVisibility;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasConfig()
        {
            return configModule != null;
        }

        @Override
        public final ModuleAssembly configModule()
        {
            return configModule;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility configVisibility()
        {
            return configVisibility;
        }
    }

    /**
     * Assembler with Identity and Configuration adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class IdentityConfig<AssemblerType> extends AssembleChecker
        implements Identifiable<AssemblerType>,
                   Configurable<AssemblerType>
    {
        private String identity;
        private ModuleAssembly configModule = null;
        private org.apache.polygene.api.common.Visibility configVisibility = org.apache.polygene.api.common.Visibility.module;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType identifiedBy( String identity )
        {
            this.identity = identity;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasIdentity()
        {
            return identity != null;
        }

        @Override
        public final String identity()
        {
            return identity;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType withConfig( ModuleAssembly configModule,
                                               org.apache.polygene.api.common.Visibility configVisibility )
        {
            this.configModule = configModule;
            this.configVisibility = configVisibility;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasConfig()
        {
            return configModule != null;
        }

        @Override
        public final ModuleAssembly configModule()
        {
            return configModule;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility configVisibility()
        {
            return configVisibility;
        }
    }

    /**
     * Assembler with Visibility, Identity and Configuation adapter.
     * @param <AssemblerType> Parameterized type of Assembler
     */
    public static abstract class VisibilityIdentityConfig<AssemblerType> extends AssembleChecker
        implements Visible<AssemblerType>,
                   Identifiable<AssemblerType>,
                   Configurable<AssemblerType>
    {
        private org.apache.polygene.api.common.Visibility visibility = org.apache.polygene.api.common.Visibility.module;
        private String identity;
        private ModuleAssembly configModule = null;
        private org.apache.polygene.api.common.Visibility configVisibility = org.apache.polygene.api.common.Visibility.module;

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType visibleIn( org.apache.polygene.api.common.Visibility visibility )
        {
            this.visibility = visibility;
            return (AssemblerType) this;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility visibility()
        {
            return visibility;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType identifiedBy( String identity )
        {
            this.identity = identity;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasIdentity()
        {
            return identity != null;
        }

        @Override
        public final String identity()
        {
            return identity;
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public final AssemblerType withConfig( ModuleAssembly configModule,
                                               org.apache.polygene.api.common.Visibility configVisibility )
        {
            this.configModule = configModule;
            this.configVisibility = configVisibility;
            return (AssemblerType) this;
        }

        @Override
        public final boolean hasConfig()
        {
            return configModule != null;
        }

        @Override
        public final ModuleAssembly configModule()
        {
            return configModule;
        }

        @Override
        public final org.apache.polygene.api.common.Visibility configVisibility()
        {
            return configVisibility;
        }
    }

    public static abstract class AssembleChecker
        implements Assembler
    {
        private boolean assembled = false;

        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            assembled = true;
        }

        @Override
        protected void finalize()
            throws Throwable
        {
            super.finalize();
            if( !assembled )
            {
                System.err.println( "WARNING!!!!!" );
                System.err.println( "############################################################################" );
                System.err.println( "##" );
                System.err.println( "##  The " + getClass().getName() + " assembler was not assembled." );
                System.err.println( "##" );
                System.err.println( "##  Expect that some functionality to be missing or incorrect." );
                System.err.println( "##" );
                if( getClass().getName().startsWith( "org.apache.polygene" ))
                {
                    System.err.println( "## When instantiating a provided Assembler, you must call the assemble(module)" );
                    System.err.println( "## method after setting the options. This was not done." );
                }
                else
                {
                    System.err.println( "## When overriding any helper class in org.apache.polygene.bootstrap.Assemblers" );
                    System.err.println( "## you must call super.assemble(module) in the assmeble(ModuleAssembly module)" );
                    System.err.println( "## method. This was not done, OR that you forgot to call assemble() method " );
                    System.err.println( "## after instantiating and setting the options." );
                }
                System.err.println( "############################################################################" );
            }
        }
    }
}
