package lombok.eclipse.dependencies.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Repository {
	@XmlElementWrapper(name = "children")
	@XmlElement(name="child")
	public List<Child> children;
	
	@XmlElementWrapper(name = "units")
	@XmlElement(name="unit")
	public List<Unit> units;
}