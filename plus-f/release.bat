call gradle clean distZip copyDependencies
%JAVA_HOME%\bin\javapackager.exe -deploy -v -nosign -native exe -outdir build\distributions -outfile Plus-F -name Plus-F -description "Plus-F" -appclass com.socialthingy.plusf.spectrum.ui.PlusF -srcdir build\libs -BappVersion=1.5.3 -BlicenseType=MIT -Bruntime=true -Bicon=package\windows\Plus-F.ico
