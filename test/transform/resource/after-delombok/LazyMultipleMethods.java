import java.lang.Deprecated;

class LazyMultipleMethods {
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private LazyMultipleMethods $lazy$thisObject;
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private String $lazy$myFavouriteDrummer;

    String myFavouriteDrummer() {
        if ($lazy$myFavouriteDrummer == null) $lazy$myFavouriteDrummer = $behavior$myFavouriteDrummer();
        return $lazy$myFavouriteDrummer;
    }

    @Deprecated
    public LazyMultipleMethods thisObject() {
        if ($lazy$thisObject == null) $lazy$thisObject = $behavior$thisObject();
        return $lazy$thisObject;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private String $behavior$myFavouriteDrummer() {
        return "Christian Vander";
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private LazyMultipleMethods $behavior$thisObject() {
        return new LazyMultipleMethods();
    }
}
