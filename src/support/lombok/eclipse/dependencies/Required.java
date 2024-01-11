package lombok.eclipse.dependencies;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Required {
	@XmlAttribute
	String namespace;
	@XmlAttribute
	String name;
	@XmlAttribute
	String range;
	@XmlAttribute
	boolean optional;
	@XmlElement
	String filter;
}
