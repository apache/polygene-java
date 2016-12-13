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

package org.apache.polygene.tools.shell;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.apache.polygene.tools.shell.create.CreateProject;
import org.apache.polygene.tools.shell.help.HelpCommand;
import org.junit.Test;

public class HelpCommandTest
{
    @Test
    public void givenTwoCommandsWhenExecutingHelpExpectExplanation(){
        HelpCommand underTest = new HelpCommand();
        List<Command> commands = Arrays.asList( underTest, new CreateProject() );
        underTest.setCommands( commands );
        ByteArrayOutputStream baos = new ByteArrayOutputStream(  );
        PrintWriter pw = new PrintWriter( baos );
        underTest.execute( null, null, pw );
        System.out.println(baos.toString());
    }
}
