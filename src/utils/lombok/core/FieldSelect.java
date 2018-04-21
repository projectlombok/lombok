package lombok.core;

public class FieldSelect {
    private final String finalPart;

    public FieldSelect(String finalPart) {
        this.finalPart = finalPart;
    }

    public String getFinalPart() {
        return finalPart;
    }
}
