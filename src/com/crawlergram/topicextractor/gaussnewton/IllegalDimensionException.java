/**
 * The author of this code: Ata Amini
 * Taken from here:
 * https://www.codeproject.com/Articles/1175992/Implementation-of-Gauss-Newton-Algorithm-in-Java
 */

package com.crawlergram.topicextractor.gaussnewton;

public class IllegalDimensionException extends Exception {

    public IllegalDimensionException() {
        super();
    }

    public IllegalDimensionException(String message) {
        super(message);
    }

}
