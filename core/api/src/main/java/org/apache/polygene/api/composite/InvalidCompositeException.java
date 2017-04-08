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
package org.apache.polygene.api.composite;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.polygene.api.structure.ModuleDescriptor;

/**
 * This exception is thrown if a Composite is invalid.
 */
public class InvalidCompositeException extends RuntimeException
{
    private static boolean aggregateProblems = true;
    private static ThreadLocal<ArrayList<InvalidCompositeException>> report = ThreadLocal.withInitial( ArrayList::new );
    private ModuleDescriptor module;
    private Class<?> primaryType;
    private Class<?> fragmentClass;
    private Type valueType;
    private Member member;
    private List<Class<?>> types;

    public static void handleInvalidCompositeType( String message, ModuleDescriptor module, Class<?> primaryType,
                                                   Class<?> fragmentClass, Type valueType, Member member,
                                                   List<Class<?>> types )
    {
        InvalidCompositeException exception = new InvalidCompositeException( message, module, primaryType,
                                                                             fragmentClass, valueType, member, types );
        if( aggregateProblems )
        {
            report.get().add( exception );
            return;
        }
        throw exception;
    }

    private InvalidCompositeException( String message, ModuleDescriptor module, Class<?> primaryType,
                                       Class<?> fragmentClass, Type valueType, Member member, List<Class<?>> types )
    {
        super( message );
        this.module = module;
        this.primaryType = primaryType;
        this.fragmentClass = fragmentClass;
        this.valueType = valueType;
        this.member = member;
        this.types = types;
    }

    @Override
    public String getMessage()
    {
        String typeNames = typesString();
        String primary = primaryType == null ? "" : "    primary: " + primaryType.toGenericString() + "\n";
        String methodName = memberString();
        String message = super.getMessage() == null ? "" : "    message: " + super.getMessage() + "\n";
        String fragment = fragmentClass == null ? "" : "    fragmentClass: " + fragmentClass.getName() + "\n";
        String valueType = this.valueType == null ? "" : "    valueType: " + this.valueType.getTypeName() + "\n";
        String module = this.module == null ? "" : "    layer: " + this.module.layer().name() + "\n    module: "
                                                   + this.module.name() + "\n";
        return message + module + primary + fragment + methodName + valueType + typeNames;
    }

    private String typesString()
    {
        if( types == null || types.size() == 0 )
        {
            return "";
        }
        return "    types: "
               + types.stream()
                      .map( Class::getSimpleName )
                      .collect( Collectors.joining( ",", "[", "]" ) )
               + "\n";
    }

    private String memberString()
    {
        if( member == null )
        {
            return "";
        }
        if( member instanceof Method )
        {
            Method method = (Method) member;
            String parameters = Arrays.stream( method.getParameters() )
                                      .map( p -> p.getType().getSimpleName() + " " + p.getName() )
                                      .collect( Collectors.joining( ", ", "(", ")" ) );
            return "    method: " + method.getReturnType().getSimpleName() + " " + method.getName() + parameters + "\n";
        }
        if( member instanceof Field )
        {
            Field field = (Field) member;
            return "    field: " + field.getType().getSimpleName() + " " + field.getName() + "\n";
        }
        return member.toString();
    }

    public static String modelReport()
    {
        if( report.get().size() > 0 )
        {
            String reportText = "\nComposition Problems Report:\n"
                                + report.get().stream()
                                        .map( Throwable::getMessage )
                                        .map( m -> m + "\n--\n" )
                                        .collect( Collectors.joining() );
            report.set( new ArrayList<>() );
            return reportText;
        }
        aggregateProblems = false;
        return null;
    }
}
