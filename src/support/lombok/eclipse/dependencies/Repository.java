package lombok.eclipse.dependencies;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Repository {
	@XmlElementWrapper(name = "children")
	@XmlElement(name="child")
	List<Child> children;
	
	@XmlElementWrapper(name = "units")
	@XmlElement(name="unit")
	List<Unit> units;
}