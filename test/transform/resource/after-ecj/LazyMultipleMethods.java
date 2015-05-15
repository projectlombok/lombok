import java.lang.Deprecated;

class LazyMultipleMethods {

    @lombok.Lazy
    String myFavouriteDrummer() {
        return "Christian" + " " + "Vander";
    }

    @lombok.Lazy
    @Deprecated
    public LazyMultipleMethods thisObject() {
        return new LazyMultipleMethods();
    }
}