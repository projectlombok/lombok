
class SafeCallIllegalUsingEnhancedForVariable {
    public SafeCallIllegalUsingEnhancedForVariable() {
        for (int intVal : getIntegerArray()) {
        }
    }

    private Integer[] getIntegerArray() {
        return new Integer[1];
    }
}