SET ECLIPSEDIR=e:\eclipse-34-rc3
CALL ant
COPY dist\lombok.jar %ECLIPSEDIR%\*.*
SET curdir=%CD%
CD %ECLIPSEDIR%
eclipsec.exe
CD %curdir%
