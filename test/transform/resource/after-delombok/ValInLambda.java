// version 8:
import java.util.function.Function;
import java.util.function.Supplier;

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

    public void inParameter() {
        final java.util.function.Function<java.util.function.Supplier<java.lang.String>, java.lang.String> foo = (Function<Supplier<String>, String>) s -> s.get();
        final java.lang.String foo2 = foo.apply(() -> {
            final java.lang.String bar = "";
            return bar;
        });
    }
}
