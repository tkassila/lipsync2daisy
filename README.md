The Lipsync2daisy Project
=========================

Team: Tuomas Kassila (Programmer), Teppo Huttunen (Lipsync2daisy and Lipsync user)

Purpose
-------
This is a Java gui application. Main purpose of this application to convert an daisy dtbook book text and audio files 
into daisy 2 or 3 audio book format files. It is also capable automatically mark-up text sentencies with right 
daisy time values. By this, the application is saving quite lot work for human reader or book editor. (Without 
this application, a human reader of a daisy book must do all sentence mark-up work and find corresponding audio files 
and voice placies by hand inside a producer tool application.)

So, that when these daisy files are generated also text sentencies are marked automatically
with audio sentence values after used text and audio files. 

Prerequisites
-------------
If you use Java 7 or a later update, you are all set.
JAVA_HOME must be set to that *parent* directory, where java.exe or java exists. And %JAVA_HOME%/bin (or $JAVA_HOME/bin) must be added 
into PATH environment variable and that PATH be set in a current users environments.

Running this application
------------------------
Extract the content of lipsync2daisyapplatest*.zip and start this script or command: lipsync2daisy.bat

How to build from source code
-----------------------------
Use eclipse ide with eclipse project files. Export a new smil2voicesmil.jar file.
(A gradle file does not work because of a groovy compiler error.)

IDE
---------------
- a project file is for eclipse


Main procedure for a user
-------------------------

To get end result (=marked daisy book) a user is needed two applications: 

Lipsync

At first, Lipsync application, which is capable to find voice times, when it has audio and text filesas 
input. It produces xml data files, which contains text and voice times of a book. When this done properly, 
a user can move on second phane:

Lipsync2daisy

After started application, create a new configuration files to a new book, or use an old one. In addition of 
dtbook text and audio files, you must copy Lipsync xml files into just created xml directory. These xml files are 
then read by this Lipsync2daisy application as input. After pressed convert button, all daisy 2 or 3 files
are generated after these data.

See documentation: Lipsync2daisy instructions_pics.pdf

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
