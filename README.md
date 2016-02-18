# Versionable DTO or Java Beans with lombok-versionable

###### Do you need versioned responses, then use lombok-versionable.

## Examples

package com.hotelbeds.architecture.code.processing.examples.lombok;
 
import lombok.Versionable;
 
public class ControllerExample {
 
    // This generate the MDC.put to save current version in a thread context
    public void myFakeController(@Versionable String version) {
    }
}


package com.hotelbeds.architecture.code.processing.examples.lombok;
 
import lombok.DataVersionable;
import lombok.SetterVersionable;
import lombok.Version;
 
@DataVersionable(versions = { Version.v1 })
public class VersionableBeanMultiVersion {
 
    // Always set ONLY for v1
    private String fieldOne;
 
    // Always set ONLY for v2
    @SetterVersionable(versions = { Version.v2 })
    private String fieldTwo;
 
    // Always set ONLY for v3
    @SetterVersionable(versions = { Version.v3 })
    private String fieldThree;
 
    // Always set ONLY for v1, v4
    @SetterVersionable(versions = { Version.v1, Version.v4 })
    private String fieldFour;
}


Original Readme, thanks lombok

Project Lombok makes java a spicier language by adding 'handlers' that know how to build and compile simple, boilerplate-free, not-quite-java code.
See LICENSE for the Project Lombok license.


To start, run:

ant -projecthelp

HINT: If you'd like to develop lombok in eclipse, run 'ant eclipse' first. It creates the necessary project infrastructure and downloads dependencies. Note that, in order to run "LombokizedEclipse.launch", you need to have "Eclipse SDK" installed.

For a list of all authors, see the AUTHORS file. 

Project Lombok was started by: 

Reinier Zwitserloot
twitter: @surial
home: http://zwitserloot.com/

Roel Spilker
twitter: @rspilker
