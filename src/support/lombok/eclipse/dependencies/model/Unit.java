package lombok.eclipse.dependencies.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Unit {
	@XmlAttribute
	public String id;
	@XmlAttribute
	public String version;
	
	@XmlElementWrapper(name = "provides")
	@XmlElement(name="provided")
	public List<Provided> provides;
	
	@XmlElementWrapper(name = "requires")
	@XmlElement(name="required")
	public List<Required> requires;

	@Override
	public String toString() {
		return id + "_" + version;
	}
}
