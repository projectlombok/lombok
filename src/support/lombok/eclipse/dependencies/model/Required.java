package lombok.eclipse.dependencies.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Required {
	@XmlAttribute
	public String namespace;
	@XmlAttribute
	public String name;
	@XmlAttribute
	public String range;
	@XmlAttribute
	public boolean optional;
	@XmlElement
	public String filter;
}
