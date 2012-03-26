@ECHO OFF

REM #
REM # Find JVM
REM #

SET JAVA=java

REM #
REM # Find JVM tools
REM #

REM TODO

REM #
REM # Find Sincerity home
REM #

SET ORIGINAL="%CD%"
REM CD /D "%0%"
SET SINCERITY_HOME="%CD%"

REM Z:\Projects\Collaborative\Sincerity\build\distribution\content

REM #
REM # Libraries
REM #

SET JVM_LIBRARIES=%SINCERITY_HOME%\bootstrap.jar

REM #
REM # Sincerity
REM #

"%JAVA%" ^
-Dsincerity.home="%SINCERITY_HOME%" ^
-Dfile.encoding=UTF-8 ^
-classpath "%JVM_LIBRARIES%" ^
com.threecrickets.bootstrap.Bootstrap %*

CD /D "%ORIGINAL%"
