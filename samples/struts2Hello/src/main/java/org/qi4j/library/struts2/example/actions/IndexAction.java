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
import java.time.Instant;
import org.apache.struts2.config.Result;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

@Conversion
@Result( value = "/jsp/index.jsp" )
public class IndexAction
    extends ActionSupport
{

    private static final long serialVersionUID = 1L;

    @Structure
    private Module module;

    private Instant now;

    public IndexAction()
    {
        now = Instant.now();
    }

    @TypeConversion( converter = "org.qi4j.library.struts2.example.converters.DateConverter" )
    public Instant getDateNow()
    {
        return now;
    }

    public String getModuleName()
    {
        return module.name();
    }

    @Override
    public String execute()
        throws Exception
    {
        now = Instant.now();
        return SUCCESS;
    }
}
