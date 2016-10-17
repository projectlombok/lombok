// version 8:
class ValInLambda {
    Runnable foo = (Runnable) () -> {
        final int i = 1;
    };

    public void easyLambda() {
        Runnable foo = (Runnable) () -> {
            final int i = 1;
        };
    }

    public void easyIntersectionLambda() {
        Runnable foo = (Runnable) () -> {
            final int i = 1;
        };
    }
}
