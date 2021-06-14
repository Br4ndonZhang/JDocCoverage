# JDocCoverage
Javadoc coverage analysis tool brought over from SourceForge.

I found a bug in it but had nowhere to report it so I imported the project into Github.
The base version is 0.10 at the time I copied it over.

## Features added which make this different from the original
So far I've:
1. mavenatized it,
1. fixed a bug where it couldn't find the default.css resource from the jar
1. added a visual completion bar under the table column for percentage complete.

## Build
The repo contains the latest stable in `dist/`, but if you want to build it yourself:
1. Clone repo
1. SWitch to repo folder
1. Build: `mvn clean install`
1. Check target folder for artiefacts

## Run
1. Change to project folder (I think there is another bug around running it from not the project folder.
1. `java -jar /path/to/dir/jdoccoverage-1.0.0-jar-with-dependencies.jar -d out_folder src/**/*.java`


## Original
* [Original SourceForge Project Page](https://sourceforge.net/projects/jdoccoverage/)
* [Original SourceForge Project Source](https://sourceforge.net/projects/jdoccoverage/files/JavaDoc%20Coverage/)

## Thanks
Thanks to https://frontendscript.com/css-progress-bar-percentage/ for the progreess bars.
