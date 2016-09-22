// version 8:

import lombok.val;

class ValInLambda {
    Runnable foo = (Runnable) () -> {
        val i = 1;
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
}
