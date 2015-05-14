class LazyMultipleMethods {
    private String $lazy$myFavouriteDrummer;

    private LazyMultipleMethods $lazy$thisObject;


    @lombok.Lazy
    String myFavouriteDrummer() {
        if (this.$lazy$myFavouriteDrummer == null) {
            this.$lazy$myFavouriteDrummer = $behavior$myFavouriteDrummer();
        }
        return this.$lazy$myFavouriteDrummer;
    }

    String $behavior$myFavouriteDrummer() {
        return "Christian Vander";
    }

    @lombok.Lazy
    LazyMultipleMethods thisObject() {
        if (this.$lazy$thisObject == null) {
            this.$lazy$thisObject = $behavior$thisObject();
        }
        return this.$lazy$thisObject;
    }

    LazyMultipleMethods $behavior$thisObject() {
        return new LazyMultipleMethods();
    }
}
