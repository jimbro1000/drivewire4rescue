# DriveWire 4 #

## How to Build ##

This project is coded to use Java 17, and uses Maven for build
and dependency management

### Pre-requisites ###

You need a suitable JDK17 package installed and configured as default
along with Maven (minimum of V3.8.x)

The build process is automated but requires internet access to download
the necessary dependencies, or a pre-populated .m2 cache

### Build ###

From the project root run maven to build, test and package the project:

```shell
mvn clean install
```

This will generate a selection of .jar files, the simplest version is a
packaged version of DriveWire without dependencies - this requires that
your java class path has all the necessary dependencies present
independently of the .jar.

The build also generates a fat version of the .jar which includes all
the dependencies packaged into it so no further downloads are needed.

The build also generates an expanded version of the slim .jar that
includes the javadocs generated from the source code.

### Reporting ###

Running `mvn clean install site` will prepare a full report on the
project. The first half is information about the project itself
and the tools used to build it.

The second half of the report details analysis of the code itself
covering checks for code duplication, violations of coding rules and
coding style, and checks for common coding errors or bad practices.

In theory the reports from the code checks should be empty because all
the possible checks result in a clean bill of health.

In addition, the report includes java docs and cross-references of the
source code. If you don't want to trawl through the source code files
this is a convenient way to view the content