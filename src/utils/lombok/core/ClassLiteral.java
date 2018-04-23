package lombok.core;

public class ClassLiteral {
    private final String className;

    public ClassLiteral(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
