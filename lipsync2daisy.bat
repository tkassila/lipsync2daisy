@echo off
rem chcp 65001

rem To show all typed characters from application to right:
rem 1. set cmd.exe (that is dosbox) to use true type font
rem 2. set chcp into 1252:
rem chcp 1252
chcp 1252
rem PAUSE
cls
Rem regexreplace ohjelma, joka hakee regex:ll‰ teksti‰ ja siirt‰‰/muuttaa niit‰ toiseen paikkaan
rem 
rem aseta merkkikoodisto
rem set argC=0
rem for %%x in (%*) do Set /A argC+=1
rem echo argC - %argC%
rem
set GROOVY_HOME=E:\Java\springsource\grails-2.1.1\lib\org.codehaus.groovy\groovy-all
set dt_exec_drive=%~d0
set dt_exec_path=%~p0
rem echo dt_exec_drive: %dt_exec_drive%
rem echo dt_exec_path: %dt_exec_path%
set execpath=%dt_exec_drive%%dt_exec_path%
rem hae onko tp komentoargumentti annettu:
set tp_annettu=
for %%F IN (%*) do if %%F == tp set tp_annettu=%%F
if %tp_annettu%. == . goto sammuta
echo shift /%argC%
set args=
for %%F IN (%*) do if not %%F == tp set args=%args%%%F
shift /%argC%
goto ohita2
:sammuta
CHCP 1252
cls
echo Asennushakemisto: %execpath%
echo.
echo Ajetaan lipsync-ohjelma:
echo.
:ohita2
rem
if %1.==. goto ohita
:ohita
set CP=%execpath%;%execpath%smil2voicesmil.jar;%execpath%lib\jna-3.0.9.jar;%execpath%lib\joda-time-2.2.jar;%execpath%lib\jl1.0.1.jar;%execpath%lib\jaudiotagger-2.0.3.jar;%execpath%lib\groovy-all-2.4.7.jar;%execpath%lib\commons-lang3-3.1.jar;%execpath%lib\tritonus_share.jar;%execpath%lib\mp3spi1.9.5.jar;%execpath%lib\resolver.jar;%classpath%;
ECHO %cp%
rem set JVMOPTIONS=
set JVMOPTIONS=-Dfile.encoding=UTF-8 -Dclient.encoding.ov=UTF-8
rem 
set cfgfile=%1
set templatedir=%2
set inputdir=%3
set outputdir=%4
set executemode=%5

rem set cfgfile=lipsync2smil.cfg
rem set inputdir=C:\Java\project\celia\smil2voicesmil\pages
rem set templatedir=C:\Java\project\celia\smil2voicesmil
rem set outputdir=C:\Java\project\celia\smil2voicesmil\tuloslipsync2smil
set LANG="fi_FI.UTF-8"
set LC_COLLATE="fi_FI.UTF-8"
set LC_CTYPE="fi_FI.UTF-8"
set LC_MESSAGES="fi_FI.UTF-8"
set LC_MONETARY="fi_FI.UTF-8"
set LC_NUMERIC="fi_FI.UTF-8"
set LC_TIME="fi_FI.UTF-8"
set LC_ALL=
java -cp %CP% %JVMOPTIONS% fi.celia.app.smil2voicesmil.Lipsync2Daisy %cfgfile% %templatedir% %inputdir% %outputdir% %executemode%
rem java -cp %CP% %JVMOPTIONS% fi.celia.app.smil2voicesmil.Lipsync2Smil C:\java\project\celia\smil2voicesmil\config\daisy2templates\lipsyn2smil2.cfg C:\TMP\koe\config C:\TMP\koe\lipsync2_xml_files C:\TMP\koe\generated_daisyfiles daisy2
rem java -cp %CP% TestCS %cfgfile% %templatedir% %inputdir% %outputdir% %5
Rem lipsync2smil.cfg C:\Java\project\celia\smil2voicesmil\pages . C:\Java\project\celia\smil2voicesmil\tuloslipsync2smil
