
Welcome to the world of Apache Polygene
   - Composite Oriented Programming on the Java platform.


This Apache Polygene™ Source Distribution contains everything you need to
create Polygene™ applications.


Polygene™ started in 2007, and is still in heavy development under the umbrella of
the Apache Polygene™ project at the Apache Software Foundation. We would like
developers around the world to participate in the advancement of this
cool, new and challenging technology. We are especially interested in
people willing to help improve the SDK, samples, tutorials, documentation
and other supporting material.

Please see https://polygene.apache.org for more information.


Licensing
---------
All Polygene™ code is licensed under an Apache License.

Third-Party Dependencies may be licensed under other terms. The only
required dependencies are SLF4J (MIT Licence), ASM (BSD Licence) and
Joda-Time (Apache Licence).

Finally, Polygene™ TestSupport depends on JUnit 4.x and its dependencies, which
is also not included in the SDK itself, as it is present among most Java
developers.


Dependencies not included
-------------------------
The source distribution contains Polygene™ sources only to keep the download
size small. The Gradle build automatically downloads needed dependencies.
If you need to go offline type `./gradlew goOffline` to ensure all needed
dependencies are cached by Gradle.

If you prefer to use a dependency management system, go to:
https://polygene.apache.org/java/latest/howto-depend-on-polygene.html


Building Apache Polygene
---------------------
To build Polygene™ from sources you only need to have a valid Java JDK >= 7
installation.

If you want to build the Polygene™ manual, then you also need valid Asciidoc
(http://www.methods.co.nz/asciidoc/) and Docbook-XSL installations.

Here is how to run a full build with checks:

    ./gradlew check assemble

Read the Polygene™ Build System tutorial for more details:
https://polygene.apache.org/java/latest/build-system.html


Thank you for trying out Apache Polygene™ and Composite Oriented Programming.


-- Apache Polygene™ Team

