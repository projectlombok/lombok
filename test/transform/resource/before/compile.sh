LIBS=""
for f in /d/workspaces/lombok/lombok/lib/test/*.jar; do
LIBS="$LIBS:$f"
done

for f in *.java; do
echo "$f"
/d/jdk-9/bin/javac \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
-J--add-opens \
-Jjdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
-cp "/d/workspaces/lombok/lombok/dist/lombok.jar$LIBS" \
-d . \
"$f"
done
