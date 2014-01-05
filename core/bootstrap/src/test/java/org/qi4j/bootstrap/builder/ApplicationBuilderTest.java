package org.qi4j.bootstrap.builder;

import org.json.JSONException;
import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ApplicationBuilderTest
{
    @Test
    public void givenJsonWhenBuildingApplicationExpectSuccess()
        throws JSONException, ActivationException, AssemblyException
    {
        ApplicationBuilder builder = ApplicationBuilder.fromJson( APPLICATION );
        Application application = builder.newApplication();
        Module module = application.findModule( "layer3", "test module" );
        TestService service = module.findService( TestService.class ).get();
        assertThat(service.sayHello(), equalTo("Hello Qi4j!"));
    }



    private static final String APPLICATION =
        "{\n" +
        "    \"name\": \"Build from JSON test.\",\n" +
        "    \"layers\": [\n" +
        "        {\n" +
        "            \"name\": \"layer1\",\n" +
        "            \"uses\": [ \"layer2\", \"layer3\"]\n" +
        "        },\n" +
        "        {\n" +
        "            \"name\": \"layer2\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"name\": \"layer3\",\n" +
        "            \"modules\" : [\n" +
        "                {\n" +
        "                    \"name\" : \"test module\",\n" +
        "                    \"assemblers\" : [\n" +
        "                            \"org.qi4j.bootstrap.builder.ApplicationBuilderTest$TestServiceAssembler\"\n" +
        "                    ]\n" +
        "                }\n" +
        "            ]\n" +
        "        }\n" +
        "    ]\n" +
        "}";

    public static class TestServiceAssembler
        implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.addServices( TestService.class );
        }
    }

    @Mixins( TestService.TestMixin.class )
    public interface TestService
    {
        String sayHello();

        class TestMixin
            implements TestService
        {

            @Override
            public String sayHello()
            {
                return "Hello Qi4j!";
            }
        }
    }
}
