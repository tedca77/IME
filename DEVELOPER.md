# For Developers
IME has been developed in Java 8 with Maven build on Intellij.  A JUNIT-based test library is also provided wih 44 separate tests. If you are interested, I can amke test files available to enable the test scripts to complete, if you do not want to set up your own. 

IME is built using open source java libraries,  including:
* ICAFE – read and writing IPTC data
* Javaxt – thumbnail creation
* Apache Commons Imaging – read and writing EXIF and Windows data
* Freemarker – for outputting HTML reports
* JavaAPI for KML generation.

OpenStreetMap does not require an API key to carry out a longitude and latitude look up.
openrouteserviceAPI does require an API key to return longitude and latitude from a post code. 

I have used jpackage to generate a Windows exe file and javapackager to create a dmg file for MacOS.  I have not done any packaging for Linux.

##Creating Windows version

Assuming you have built a jar file using IntelliJ Build.

'''
jpackage --name IME --input "D:/ImageCatalogue/out/artifacts/ImageMetadataEnhancer_jar" 
--main-jar ImageMetadataEnhancer.jar --main-class IME.IMEMethods --win-console 
--dest "D:/ImageCatalogue/out/artifacts/ImageMetadataEnhancer_jar"  --java-options -Dfile.encoding=UTF8
'''

##Creating MacOS version

If anyone would like to build this project for MacOS, I can provide additional information here.



Thanks to all developers for working on the open source libraries that have enabled this application. I would welcome additional contributions to this. 

Thanks also to OpenStreetMap, openroutservice and all the contributors for providing excellent API services.  

Ted Carroll
January 2023
London, UK.
