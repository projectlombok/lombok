package lombok.installer.eclipse;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeLocation;
import lombok.installer.IdeLocationProvider;
import lombok.installer.IdeFinder.OS;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(IdeLocationProvider.class)
public class STSLocationProvider extends EclipseLocationProvider {
	@Override protected List<String> getEclipseExecutableNames() {
		return Arrays.asList("sts.app", "sts.exe", "stsc.exe", "sts");
	}
	
	@Override protected String getIniName() {
		return "STS.ini";
	}
	
	@Override protected IdeLocation makeLocation(String name, File ini) throws CorruptedIdeLocationException {
		return new STSLocation(name, ini);
	}
	
	@Override protected String getMacAppName() {
		return "STS.app";
	}
	
	@Override protected String getUnixAppName() {
		return "STS";
	}
	
	@Override public Pattern getLocationSelectors(OS os) {
		switch (os) {
		case MAC_OS_X:
			return Pattern.compile("^(sts|sts\\.ini|sts\\.app)$", Pattern.CASE_INSENSITIVE);
		case WINDOWS:
			return Pattern.compile("^(stsc?\\.exe|sts\\.ini)$", Pattern.CASE_INSENSITIVE);
		default:
		case UNIX:
			return Pattern.compile("^(sts|sts\\.ini)$", Pattern.CASE_INSENSITIVE);
		}
	}
}
