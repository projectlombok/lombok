package lombok.mavenplugin;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.javac.JavacCompiler;

public class LombokedJavacCompiler extends JavacCompiler {
	@Override public String getCompilerId() {
		return "lombok";
	}
	
	private static final String[] LOMBOK_ADD_OPENS_C_S_T_J = {"code", "comp", "file", "main", "model", "parser", "processing", "tree", "util"};
	
	@Override public CompilerResult performCompile(CompilerConfiguration conf) throws CompilerException {
		conf.setFork(true);
		Map<String, String> out = new HashMap<String, String>();
		Map<String, String> map = conf.getCustomCompilerArgumentsAsMap();
		if (map != null) out.putAll(map);
		for (String n : LOMBOK_ADD_OPENS_C_S_T_J) out.put("-J--add-opens=jdk.compiler/com.sun.tools.javac." + n + "=ALL-UNNAMED", null);
		conf.setCustomCompilerArgumentsAsMap(out);
		return super.performCompile(conf);
	}
}
