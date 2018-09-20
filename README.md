# NF DEV
The goal of this project is that of building a VNF model starting from its actual implementation and building a translations vs SymNet and Verigraph.  

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

## Note
Eclipse version: Eclipse Java EE IDE for Web Developers.
Version: Photon Release (4.8.0)
Build id: 20180619-1200
Java version: "1.8.0_131" (Java HotSpot 64-Bit Server VM (build 25.131-b11, mixed mode)

