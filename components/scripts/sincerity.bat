REM @ECHO OFF

SET ORIGINAL="%CD%"
REM CD /D "%0%\..\.."

SET SINCERITY_HOME=Z:\Projects\Collaborative\Sincerity\build\distribution\content
SET CLASSPATH=%SINCERITY_HOME%\bootstrap.jar

SET JAVA=java

"%JAVA%" ^
-Dsincerity.home="%SINCERITY_HOME%" ^
-Dfile.encoding=UTF-8 ^
-classpath "%CLASSPATH%" ^
com.threecrickets.bootstrap.Bootstrap %*

CD /D "%ORIGINAL%"
