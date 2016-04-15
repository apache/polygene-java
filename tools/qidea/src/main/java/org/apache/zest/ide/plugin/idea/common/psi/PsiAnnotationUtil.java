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

package org.apache.zest.ide.plugin.idea.common.psi;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public class PsiAnnotationUtil
{
    @NotNull
    public static List<PsiAnnotationMemberValue> getAnnotationDefaultParameterValue( @Nullable PsiAnnotation annotation )
    {
        if( annotation == null )
        {
            return emptyList();
        }

        List<PsiAnnotationMemberValue> defaultParameterValues = new LinkedList<PsiAnnotationMemberValue>();

        PsiAnnotationParameterList list = annotation.getParameterList();
        PsiNameValuePair[] attributes = list.getAttributes();
        for( PsiNameValuePair valuePair : attributes )
        {
            String parameterName = valuePair.getName();
            if( parameterName == null || PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME.equals( parameterName ) )
            {
                PsiAnnotationMemberValue value = valuePair.getValue();
                if( value == null )
                {
                    continue;
                }

                if( value instanceof PsiArrayInitializerMemberValue )
                {
                    // If It's an array
                    PsiArrayInitializerMemberValue valueWrapper = (PsiArrayInitializerMemberValue) value;
                    PsiAnnotationMemberValue[] values = valueWrapper.getInitializers();
                    for( PsiAnnotationMemberValue psiAnnotationMemberValue : values )
                    {
                        if( psiAnnotationMemberValue != null )
                        {
                            defaultParameterValues.add( psiAnnotationMemberValue );
                        }
                    }
                }
                else
                {
                    // If there's only one value
                    defaultParameterValues.add( value );
                }

                break;
            }
        }

        return defaultParameterValues;
    }

    @Nullable
    public static PsiJavaCodeReferenceElement getClassReference( @NotNull PsiAnnotationMemberValue value )
    {
        if( value instanceof PsiClassObjectAccessExpression )
        {
            PsiClassObjectAccessExpression objectAccessExpression = (PsiClassObjectAccessExpression) value;
            PsiTypeElement typeElement = objectAccessExpression.getOperand();
            return typeElement.getInnermostComponentReferenceElement();
        }

        return null;
    }
}
