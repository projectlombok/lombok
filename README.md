# Project Lombok makes java a spicier language by adding 'handlers' that know how to build and compile simple, boilerplate-free, not-quite-java code.
See LICENSE for the Project Lombok license.


## To start, run:

ant -projecthelp

HINT: If you'd like to develop lombok in eclipse, run 'ant eclipse' first. It creates the necessary project infrastructure and downloads dependencies. Note that, in order to run "LombokizedEclipse.launch", you need to have "Eclipse SDK" installed.

For a list of all authors, see the AUTHORS file. 

Project Lombok was started by: 

Reinier Zwitserloot
twitter: @surial
home: http://zwitserloot.com/

Roel Spilker


## Qadium Changelog from Original Repo

* 5/25/17: Changed annotation type from CLASS to RUNTIME retention policy to support finding annotations via reflection.
* 5/25/17: Added nexus deploy capability.  See below for instructions

## Rebuild and Redeploy Instructions

A one-time setup must be called via:
```bash
ant setupJavaOracle8TestEnvironment
```

Once done, rebuilding source is done via:

```bash
ant clean dist
```

### Deploying a new snapshot version

Snapshot versions must end with '-VERSION' like:

```bash
ant maven -Dartifact.type=snapshot -Dlombok.version=1.17.19-qadium-SNAPSHOT
```

### Deploying a new release version

```bash
ant maven -Dartifact.type=release -Dlombok.version=1.17.19-qadium
```