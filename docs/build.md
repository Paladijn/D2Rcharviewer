# Build on your own
While a zip with an executable is offered, you can also build the software on your own. To run it non-native, grab any JVM (Adoptium, Azul, GraalVM are great) and run the application using
```shell
.\mvnw.cmd package quarkus:run
```
You can also run in developer mode with quarkus:dev, but this will impact performance.

If you wish to build the .exe by yourself, follow the instructions [here](https://www.graalvm.org/latest/docs/getting-started/windows/) and install both GraalVM and Visual Studion build tools/Windows SDK if you don't have them.
You can then build the .exe with:
```shell
.\mvnw.cmd package -Dnative
```
This will take about 1-3 minutes depending on your cpu and memory.  
The resulting executable will be available in the target folder.