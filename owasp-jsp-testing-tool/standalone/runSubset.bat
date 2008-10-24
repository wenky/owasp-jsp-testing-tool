@echo off

@REM Clear the lib env var as it can hose tomcat
SET lib= 

@REM Set env vars for tomcat and java, use PWD as some machines don't have
@REM \. on their path
set PWD=%cd%
set JAVA_HOME=%PWD%\java
set TOMCAT_HOME=%PWD%\tomcat

set /p TAG= Tag to tester(ex. commandLink or outputLabel): 

echo Starting JSP Testing Tool...

"%JAVA_HOME%\bin\java.exe" -classpath "%PWD%\build\classes;%PWD%\lib\commons\commons-codec-1.3.jar;%PWD%\lib\commons\commons-httpclient-3.1.jar;%PWD%\lib\commons\commons-io-1.4.jar;%PWD%\lib\jakarta-ecs\ecs-1.4.2.jar;%PWD%\lib\owasp-esapi\owasp-esapi-java-1.3.jar;%PWD%\lib\velocity\commons-collections-3.2.1.jar;%PWD%\lib\velocity\commons-lang-2.4.jar;%PWD%\lib\velocity\jakarta-oro-2.0.8.jar;%PWD%\lib\velocity\velocity-1.5.jar;%TOMCAT_HOME%\bin\tomcat-juli.jar;%TOMCAT_HOME%\lib\annotations-api.jar;%TOMCAT_HOME%\lib\catalina.jar;%TOMCAT_HOME%\lib\catalina-ant.jar;%TOMCAT_HOME%\lib\catalina-ha.jar;%TOMCAT_HOME%\lib\catalina-tribes.jar;%TOMCAT_HOME%\lib\commons-logging-1.1.1.jar;%TOMCAT_HOME%\lib\el-api.jar;%TOMCAT_HOME%\lib\jasper.jar;%TOMCAT_HOME%\lib\jasper-el.jar;%TOMCAT_HOME%\lib\jasper-jdt.jar;%TOMCAT_HOME%\lib\jsp-api.jar;%TOMCAT_HOME%\lib\servlet-api.jar;%TOMCAT_HOME%\lib\tomcat-coyote.jar;%TOMCAT_HOME%\lib\tomcat-dbcp.jar;%TOMCAT_HOME%\lib\tomcat-i18n-es.jar;%TOMCAT_HOME%\lib\tomcat-i18n-fr.jar;%TOMCAT_HOME%\lib\tomcat-i18n-ja.jar" org.owasp.jsptester.tester.JspTester "%PWD%\resources\html_basic.tld" "%PWD%\resources\html_basic.tpx" "%PWD%\output" %TAG%
 
pause