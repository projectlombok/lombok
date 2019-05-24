// version 8:

import lombok.val;

class ValInLambda {
    Runnable foo = (Runnable) () -> {
        val i = 1;
        lombok.val foo = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : System.out::println;
    };

    public void easyLambda() {
        Runnable foo = (Runnable) () -> {
            val i = 1;
        };
    }

    public void easyIntersectionLambda() {
        Runnable foo = (Runnable) () -> {
            val i = 1;
        };
    }
    
    public void easyLubLambda() {
        Runnable foo = (Runnable) () -> {
            lombok.val fooInner = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : System.out::println;
        };
    }
}
