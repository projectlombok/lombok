SET ECLIPSEDIR=c:\eclipse
CALL ant
COPY dist\lombok.jar %ECLIPSEDIR%\*.*
SET curdir=%CD%
CD %ECLIPSEDIR%
eclipsec.exe
CD %curdir%
