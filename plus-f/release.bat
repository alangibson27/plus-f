call ..\gradlew clean installDist
call "C:\Program Files\Java\jdk11\bin\jlink.exe" --verbose --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml,jdk.unsupported --output build\install\plus-f\runtime
call "C:\Program Files (x86)\Inno Setup 5\iscc.exe" package\windows\plus-f.iss /Obuild /FPlus-F
call aws s3 cp build/Plus-F.exe s3://download.socialthingy.com/Plus-F.exe
