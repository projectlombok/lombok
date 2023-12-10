package lombok.eclipse.dependencies;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Unit {
	@XmlAttribute
	String id;
	@XmlAttribute
	String version;
	
	@XmlElementWrapper(name = "provides")
	@XmlElement(name="provided")
	List<Provided> provides;
	
	@XmlElementWrapper(name = "requires")
	@XmlElement(name="required")
	List<Required> requires;

	@Override
	public String toString() {
		return id + "_" + version;
	}
}
