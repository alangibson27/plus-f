call ..\gradlew clean installDist
call jlink --verbose --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml,jdk.unsupported --output build\install\plus-f\runtime
call iscc.exe package\windows\plus-f.iss /Obuild /FPlus-F
call aws s3 cp build/Plus-F.exe s3://download.socialthingy.com/Plus-F.exe
