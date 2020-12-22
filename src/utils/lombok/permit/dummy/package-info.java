/**
 * This package recreates the type hierarchy of {@code java.lang.reflect.AccessibleObject} and friends (such as {@code java.lang.reflect.Method});
 * its purpose is to allow us to ask {@code sun.misc.internal.Unsafe} about the exact offset of the {@code override} field of {@code AccessibleObject};
 * asking about that field directly doesn't work after jdk14, presumably because the fields of AO are expressly hidden somehow.
 * 
 *  NB: It's usually 12, on the vast majority of OS, VM, and architecture combos.
 */
package lombok.permit.dummy;
