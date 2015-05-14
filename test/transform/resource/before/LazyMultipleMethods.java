class LazyMultipleMethods {

    @lombok.Lazy
    String myFavouriteDrummer() {
        return "Christian" + " " + "Vander";
    }

    @lombok.Lazy
    LazyMultipleMethods thisObject() {
        return new LazyMultipleMethods();
    }
}