# NF DEV
The goal of this project is that of building a VNF model starting from its actual implementation and building a translations vs SymNet and Verigraph.  

## Installation (Windows)
(1) Make sure JDK is installed, and “JAVA_HOME” variable is added as Windows environment variable. <br>
(2) Intall Maven:

	- Download Apache Maven (Official website: http://maven.apache.org/download.cgi) 
	- Unzip it to the folder you want to install Maven. 
	- Add both M2_HOME and MAVEN_HOME variables in the Windows environment, and point it to your Maven folder
	- Update PATH variable, append Maven bin folder – %M2_HOME%\bin
	- (That’s all, installation is NOT required!)

You can verify whether Maven is successfully installed, 
use the following command:
	
	- mvn --version

The result should be similar to: 

	Apache Maven 3.2.2 (45f7c06d68e745d05611f7fd14efb6594181933e; 2014-06-17T21:51:42+08:00)
	Maven home: C:\Program Files\Apache\maven
	Java version: 1.7.0_65, vendor: Oracle Corporation
	Java home: C:\Program Files\Java\jdk1.7.0_65\jre
	Default locale: en_US, platform encoding: Cp1252
	OS name: "windows 8.1", version: "6.3", arch: "amd64", family: "windows"
	
(3) Import the project in your Eclipse workspace<br>
(4) Run the target 'generate-bindings' of build.xml ANT in order to generate JAXB logical expression classes from the schema LogicalExpression.xsd<br>

## Installation (Unix)
(1) Install jdk1.8.X_YY from the command line. <br>
(2) Intall Maven<br>
Open the terminal and follow the instructions below:

	- sudo apt-get update && sudo apt-get upgrade
	- sudo apt-get -y install maven
	- export M2_HOME=/opt/maven
	- export MAVEN_HOME=/opt/maven
	- export PATH=${M2_HOME}/bin:${PATH}
	
After the installation is complete, you can verify whether Maven is successfully installed, 
use the following command:
	
	- mvn --version

The result should be similar to: 

	Apache Maven 3.3.9
	Maven home: /usr/share/maven
	Java version: 1.8.0_151, vendor: Oracle Corporation
	Java home: /usr/lib/jvm/java-8-openjdk-amd64/jre
	Default locale: en_US, platform encoding: ANSI_X3.4-1968
	OS name: "linux", version: "2.6.32-042stab127.2", arch: "amd64", family: "unix"	

(3) Import the project in your Eclipse workspace<br>
(4) Run the target 'generate-bindings' of build.xml ANT in order to generate JAXB logical expression classes from the schema LogicalExpression.xsd<br>

## Tool
The tool is composed by different parts:

	- a library that can be used to write the implementation of a generic VNF (package it.polito.nfdev.lib);
	- a set of verification-oriented functions and annotations in order to enrich the VNF implementation and aid the extraction of logic formulas;
	- an example on how to write the code of a real NAT function (package it.polito.nfdev.nat)
	- a translator vs Verigraph
	- a translator vs SymNet 
	
Folders:

	- src: there are package for: Library, NFs, SymNet Translator, Verigraph Translation, Verigraph Test execution. 
	- generated: generated logical expression classes by jaxb
	- doc: documentation of the project
	- lib: library dependencies collected for Ant 
	- nfSymNetJava: folder to output classes of the 1th phase of SymNet Translation. (Parser)
	- nfSymNetScala/input: folder to output classes of the 2dh phase of SymNetTranslation (mvn plug-in)
	- nfSymNetScala/output folder to output classes of the 3th phase of SymNetTranslation (Post-Process)
	- xsd/java: folder to output classes of Verigraph traslation
	- xsd/txt: there are the NFs rules in a textual form (make by Parser)
	- xsd: there are the NFs in XML format and the XML-schema.

Files:

	- build.xml: Script for generating jaxb logical expression classes
	- builer.xml: Script for generating NFs compatible with SymNet
	- pom.xml: resolve dependencies of Maven to run javatoscala plug-in	 

## RUN (builder.xml)
Description of properties:

	- nfInput.parser = put here the path and the name of the VNF you want to translate into a general XML format
	- nfInput.postProcess = put here the path and the name of the VNF.scala you want to translate in SymNet
	- nfOutput.postProcess = put here the path and the name of the VNF.scala you want to generate
Description of all Target:

	-prepare: Generating new build folders
	-compile: Compile Java sources
	-run_parser<arg1><arg2>: Generating the XML model of the <arg1> VNF,
								If arg2=v than translate the XML model in Verigraph model
								If arg2=s than translate the XML model in SymNet(1th phase)
	-run_mvn: Running the Maven plugin in order to translate the Java model in Scala model (2dh phase of SymNetTranslation)
	-run_postParser<arg1><arg2>: Running the 3th phase of SymNetTranslation to rewrite the Scala model.
									<arg1> the Scala file to rewrite
									<arg2> the output Scala file
	-clean: Delete old build and directories
	-NFs_SymNet_generation: Running all the process in order to generate the SymNet model. Running all target.
## Note
Eclipse version: Eclipse Java EE IDE for Web Developers.
Version: Photon Release (4.8.0)
Build id: 20180619-1200
Java version: "1.8.0_131" (Java HotSpot 64-Bit Server VM (build 25.131-b11, mixed mode)

