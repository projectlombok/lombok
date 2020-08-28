package lombok.website;

import static spark.Spark.*;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import spark.Request;
import spark.Response;
import spark.Route;

public class RunSite {
	private static final int DEFAULT_PORT = 4569;
	private final Path base;
	
	public RunSite(Path base) {
		this.base = base;
	}
	
	public static void main(String[] args) throws Exception {
		boolean open = args.length > 1 && args[1].equals("open");
		new RunSite(Paths.get(args[0])).go(open);
	}
	
	private void go(boolean open) throws Exception {
		port(DEFAULT_PORT);
		get("/", serve("main.html"));
		get("/setup/overview", serve("setup/main.html"));
		get("/setup", serve("setup/main.html"));
		get("/features", serve("features/index.html"));
		get("/features/all", serve("features/index.html"));
		get("/features/experimental/all", serve("features/experimental/index.html"));
		get("/features/experimental", serve("features/experimental/index.html"));
		
		serveDir("/", base);
		
		System.out.println("Serving page from " + base + " -- hit enter to stop");
		if (open) Opener.open("http://localhost:" + DEFAULT_PORT + "/");
		System.in.read();
		System.exit(0);
	}
	
	private void serveDir(String sub, Path dir) throws IOException {
		DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
		try {
			for (Path c : ds) {
				String n = c.getFileName().toString();
				if (n.equals(".") || n.equals("..")) continue;
				if (Files.isDirectory(c)) {
					serveDir(sub + n + "/", c);
					continue;
				}
				String rel = base.relativize(c).toString();
				get(sub + n, serve(rel));
				if (n.endsWith(".html")) get(sub + n.substring(0, n.length() - 5), serve(rel));
			}
		} finally {
			ds.close();
		}
	}
	
	private static class Opener {
		public static void open(String url) throws Exception {
			Desktop.getDesktop().browse(new URI(url));
		}
	}
	
	private Route serve(final String path) {
		final Path tgt = base.resolve(path);
		
		return new Route() {
			@Override public Object handle(Request req, Response res) throws Exception {
				res.type(mapMime(path));
				return Files.readAllBytes(tgt);
			}
		};
	}
	
	private String mapMime(String path) {
		if (path.endsWith(".css")) return "text/css; charset=UTF-8";
		if (path.endsWith(".js")) return "text/javascript; charset=UTF-8";
		if (path.endsWith(".png")) return "image/png";
		if (path.endsWith(".gif")) return "image/gif";
		if (path.endsWith(".jpg")) return "image/jpeg";
		if (path.endsWith(".mp4")) return "video/mp4";
		if (path.endsWith(".m4v")) return "video/mp4";
		if (path.endsWith(".ogv")) return "video/ogg";
		if (path.endsWith(".webm")) return "video/webm";
		if (path.endsWith(".ico")) return "image/x-icon";
		if (path.endsWith(".pdf")) return "application/pdf";
		if (path.endsWith(".json")) return "application/json";
		if (path.endsWith(".xml")) return "text/xml";
		if (path.endsWith(".woff")) return "font/woff";
		if (path.endsWith(".woff2")) return "font/woff2";
		if (path.endsWith(".html")) return "text/html; charset=UTF-8";
		return "text/plain; charset=UTF-8";
	}
}
