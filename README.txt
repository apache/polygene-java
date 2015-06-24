
Welcome to the world of Apache Zest Qi4j
   - Composite Oriented Programming on the Java platform.


This Apache Zest Qi4j Source Distribution contains everything you need to
create Qi4j applications.


Qi4j started in 2007, and is still in heavy development under the umbrella of
the Apache Zest project at the Apache Software Foundation. We would like
developers around the world to participate in the advancement of this
cool, new and challenging technology. We are especially interested in
people willing to help improve the SDK, samples, tutorials, documentation
and other supporting material.

Please see https://zest.apache.org for more information.


Licensing
---------
All Qi4j code is licensed under an Apache License.

Third-Party Dependencies may be licensed under other terms. The only
required dependencies are SLF4J (MIT Licence), ASM (BSD Licence) and
Joda-Time (Apache Licence).

Finally, Qi4j TestSupport depends on JUnit 4.x and its dependencies, which
is also not included in the SDK itself, as it is present among most Java
developers.


Dependencies not included
-------------------------
The source distribution contains Qi4j sources only to keep the download
size small. The Gradle build automatically downloads needed dependencies.
If you need to go offline type `./gradlew gooffline` to ensure all needed
dependencies are cached by Gradle.

If you prefer to use a dependency management system, go to:
https://zest.apache.org/qi4j/latest/howto-depend-on-qi4j.html


Building Apache Zest Qi4j
-------------------------
To build Qi4j from sources you only need to have a valid Java JDK >= 7
installation.

This document the Qi4j build system and its usage:
https://zest.apache.org/qi4j/latest/build-system.html


Thank you for trying out Apache Zest Qi4j and Composite Oriented Programming.


-- Apache Zest Qi4j Team

