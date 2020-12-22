@lombok.Getter
@lombok.Setter
class JavadocMultiline {
    /**
     * This is a list of booleans.
     *
     * @param booleans A list of booleans to set for this object. This is a Javadoc param that is
     *        long enough to wrap to multiple lines.
     * @return A list of booleans to set for this object. This is a Javadoc return that is long
     *         enough to wrap to multiple lines.
     */
    private java.util.List<Boolean> booleans;
    
    
    /**
     * This is a list of booleans.
     *
     * @param booleans A list of booleans to set for this object. This is a Javadoc param that is
     *        long enough to wrap to multiple lines.
     */
    private java.util.List<Boolean> booleans2;
}
