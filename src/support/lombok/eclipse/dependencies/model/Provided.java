package lombok.eclipse.dependencies.model;

import javax.xml.bind.annotation.XmlAttribute;

public class Provided {
	@XmlAttribute
	public String namespace;
	@XmlAttribute
	public String name;
	@XmlAttribute
	public String version;
}
