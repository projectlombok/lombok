package lombok.eclipse.dependencies;

import javax.xml.bind.annotation.XmlAttribute;

public class Provided {
	@XmlAttribute
	String namespace;
	@XmlAttribute
	String name;
	@XmlAttribute
	String version;
}
