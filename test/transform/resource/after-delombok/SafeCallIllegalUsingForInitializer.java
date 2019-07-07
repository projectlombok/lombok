
class SafeCallIllegalUsingForInitializer {
    public SafeCallIllegalUsingForInitializer() {
        for (int i2 = nullIndex(); i2 < 0; i2++) {
        }
    }

    public Integer nullIndex() {
        return null;
    }
}