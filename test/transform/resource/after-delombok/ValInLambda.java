// version 8:
class ValInLambda {
    Runnable foo = (Runnable) () -> {
        final int i = 1;
        final java.lang.Runnable foo = (System.currentTimeMillis() > 0) ? (Runnable) () -> {
        } : System.out::println;
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
    
    public void easyLubLambda() {
        Runnable foo = (Runnable) () -> {
            final java.lang.Runnable fooInner = (System.currentTimeMillis() > 0) ? (Runnable) () -> {
            } : System.out::println;
        };
    }
}
