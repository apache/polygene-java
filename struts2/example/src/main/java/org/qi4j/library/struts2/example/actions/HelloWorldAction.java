/*
 * $Id: RequestUtils.java 394468 2006-04-16 12:16:03Z tmjee $
 *
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.struts2.example.actions;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.conversion.annotations.Conversion;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;
import org.apache.struts2.config.Result;
import org.apache.struts2.config.Results;

import java.util.Date;

@Validation
@Conversion
@Results( { @Result( name = "input", value = "/jsp/index.jsp" ), @Result( value = "/jsp/helloWorld.jsp" ) } )
public class HelloWorldAction extends ActionSupport
{

    private static final long serialVersionUID = 1L;

    private Date now;
    private String name;

    @TypeConversion( converter = "org.qi4j.library.struts2.example.converters.DateConverter" )
    @RequiredFieldValidator( message = "Please enter the date" )
    public void setDateNow( Date dateNow )
    {
        now = dateNow;
    }

    public Date getDateNow()
    {
        return now;
    }

    @RequiredStringValidator( message = "Please enter a name", trim = true )
    public void setName( String userName )
    {
        name = userName;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String execute()
        throws Exception
    {
        return SUCCESS;
    }
}
