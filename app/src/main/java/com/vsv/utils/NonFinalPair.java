package com.vsv.utils;

import android.util.Pair;

public class NonFinalPair<F, S> extends Pair<F, S> {

    public F nonFinalFirstValue;

    public S nonFinalSecondValue;

    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public NonFinalPair(F first, S second) {
        super(first, second);
        this.nonFinalFirstValue = first;
        this.nonFinalSecondValue = second;
    }
}
