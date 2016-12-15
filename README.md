The Lipsync2daisy Project
=========================

Team: Tuomas Kassila (Programmer), Teppo Huttunen (Lipsync user)

Prerequisites
-------------
If you use Java 7 or a later update, you are all set.
JAVA_HOME must be set to that directory, where java.exe or java exists. And JAVA_HOME/bin must be added 
into PATH environment variable and that PATH be set in a current users environments.

Running this application
------------------------
Unzip lipsync2daisyapplatest.zip and start this script or command: lipsync2daisy.bat

How to build
------------
> gradle -b build.gradle jar or build.bat or build.sh

IDE
---------------
- a project file is for eclipse

Purpose
-------
This is a Java gui application to convert an daisy dtbook book text and audio files into daisy 2 or 3 audio book
format files. It is also capable automatically mark-up text sentencies with right daisy time values. By this,
the application save quite lot work for human reader or book editor. (Without this application, a human reader 
of a daisy book must do all sentence mark-up and find corresponding audio files and placies by hand.)

So, that when these daisy files are generated also text sentencies are marked automatically
with audio sentence values after used text and audio files. 

These audio files readed by Lipsync application. The lipsync then produces xml files. These xml files are then read
by this Lipsync2daisy application as input.

Project layout
--------------
                                                        
Demos
-----

General approach
----------------

Dirty state
-----------

Thread Safety
-------------

Programming mode
----------------

Design decisions
----------------
