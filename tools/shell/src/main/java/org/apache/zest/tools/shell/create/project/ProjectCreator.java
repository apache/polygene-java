package org.apache.zest.tools.shell.create.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface ProjectCreator
{
    void create( String projectName,
                 File projectDir,
                 Map<String, String> properties
    )
        throws IOException;
}
