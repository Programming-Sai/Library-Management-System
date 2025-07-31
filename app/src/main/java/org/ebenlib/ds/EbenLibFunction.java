package org.ebenlib.ds;

@FunctionalInterface
public interface EbenLibFunction<A, R> {
    R apply(A input);

    // Optional: Compose two functions (this ◦ before)
    default <V> EbenLibFunction<V, R> compose(EbenLibFunction<? super V, ? extends A> before) {
        return (V v) -> apply(before.apply(v));
    }

    // Optional: Chain this with another function (after ◦ this)
    default <V> EbenLibFunction<A, V> andThen(EbenLibFunction<? super R, ? extends V> after) {
        return (A a) -> after.apply(apply(a));
    }

    // Optional: Identity function
    static <T> EbenLibFunction<T, T> identity() {
        return t -> t;
    }
}
