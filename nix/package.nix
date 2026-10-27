{ lib, stdenvNoCC, fetchurl, linkFarm, jdk25, ant, python3 }: let
  lock = builtins.fromJSON (builtins.readFile ./dependencies.json);
  mirror = linkFarm "lombok-dependencies" (map (item: {
    name = builtins.replaceStrings [ "://" ] [ "/" ] item.url;
    path = fetchurl { inherit (item) url sha256; };
  }) lock.downloads);
in stdenvNoCC.mkDerivation {
  pname = "lombok";
  version = "local";
  src = lib.fileset.toSource {
    root = ../.;
    fileset = lib.fileset.unions [
      ../src ../test ../buildScripts ../build.xml ../doc ../website
      ../LICENSE ../AUTHORS ../README.md
    ];
  };
  patches = [ ./reproducible-spi.patch ];
  nativeBuildInputs = [ jdk25 ant python3 ];
  JAVA_HOME = jdk25;
  SOURCE_DATE_EPOCH = "315532800";
  TZ = "UTC";
  JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Duser.timezone=UTC";
  ANT_OPTS = "-Xmx1200m";

  postPatch = ''
    python3 ${./dependencies.py} mirror ${./dependencies.json} ${mirror}
  '';
  buildPhase = ''
    runHook preBuild
    ant -Dant.tstamp.now="$SOURCE_DATE_EPOCH" dist
    runHook postBuild
  '';
  doCheck = true;
  checkPhase = ''
    runHook preCheck
    ant -Dant.tstamp.now="$SOURCE_DATE_EPOCH" test.javacCurrent
    runHook postCheck
  '';
  installPhase = ''
    runHook preInstall
    install -Dm644 dist/lombok.jar "$out/share/java/lombok.jar"
    runHook postInstall
  '';
  meta.license = lib.licenses.mit;
}
