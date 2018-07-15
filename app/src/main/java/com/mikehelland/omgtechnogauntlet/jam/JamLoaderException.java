package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 4/29/18.
 */

class JamLoaderException extends Exception {
    String message;
    JamLoaderException(String message) {
        this.message = message;
    }
}
