
Welcome to the world of Apache Zest
   - Composite Oriented Programming on the Java platform.


This Apache Zest™ Source Distribution contains everything you need to
create Zest™ applications.


Zest™ started in 2007, and is still in heavy development under the umbrella of
the Apache Zest™ project at the Apache Software Foundation. We would like
developers around the world to participate in the advancement of this
cool, new and challenging technology. We are especially interested in
people willing to help improve the SDK, samples, tutorials, documentation
and other supporting material.

Please see https://zest.apache.org for more information.


Licensing
---------
All Zest™ code is licensed under an Apache License.

Third-Party Dependencies may be licensed under other terms. The only
required dependencies are SLF4J (MIT Licence), ASM (BSD Licence) and
Joda-Time (Apache Licence).

Finally, Zest™ TestSupport depends on JUnit 4.x and its dependencies, which
is also not included in the SDK itself, as it is present among most Java
developers.


Dependencies not included
-------------------------
The source distribution contains Zest™ sources only to keep the download
size small. The Gradle build automatically downloads needed dependencies.
If you need to go offline type `./gradlew gooffline` to ensure all needed
dependencies are cached by Gradle.

If you prefer to use a dependency management system, go to:
https://zest.apache.org/java/latest/howto-depend-on-zest.html


Building Apache Zest
---------------------
To build Zest™ from sources you only need to have a valid Java JDK >= 7
installation.

If you want to build the Zest™ manual, then you also need a valid Asciidoc
(http://www.methods.co.nz/asciidoc/) installation.

This document the Zest™ build system and its usage:
https://zest.apache.org/java/latest/build-system.html


Thank you for trying out Apache Zest™ and Composite Oriented Programming.


-- Apache Zest™ Team

