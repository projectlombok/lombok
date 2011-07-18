package lombok.core.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public class DebugSnapshotStore {
	public static final DebugSnapshotStore INSTANCE = new DebugSnapshotStore();
	
	private final Map<CompilationUnitDeclaration, List<DebugSnapshot>> map =
			new WeakHashMap<CompilationUnitDeclaration, List<DebugSnapshot>>();
	
	public void snapshot(CompilationUnitDeclaration owner, String message, Object... params) {
		DebugSnapshot snapshot = new DebugSnapshot(owner, 1, message, params);
		List<DebugSnapshot> list;
		
		synchronized (map) {
			list = map.get(owner);
			if (list == null) {
				list = new ArrayList<DebugSnapshot>();
				map.put(owner, list);
			}
			list.add(snapshot);
		}
	}
	
	public void print(CompilationUnitDeclaration owner, String message, Object... params) {
		List<DebugSnapshot> list;
		
		synchronized (map) {
			snapshot(owner, message == null ? "Printing" : message, params);
			list = new ArrayList<DebugSnapshot>();
			list.addAll(map.get(owner));
		}
		
		Collections.sort(list);
		int idx = 1;
		System.out.println("---------------------------");
		for (DebugSnapshot snapshot : list) {
			System.out.printf("%3d: %s\n", idx++, snapshot.shortToString());
		}
		System.out.println("******");
		idx = 1;
		for (DebugSnapshot snapshot : list) {
			System.out.printf("%3d: %s", idx++, snapshot.toString());
		}
	}
}
