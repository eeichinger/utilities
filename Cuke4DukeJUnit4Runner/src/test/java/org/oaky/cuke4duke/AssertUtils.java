package org.oaky.cuke4duke;

import com.sun.javaws.exceptions.InvalidArgumentException;

/**
 * Created by IntelliJ IDEA.
 * User: b1exe04
 * Date: 3/9/11
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
class AssertUtils {
    static void notNull(Object arg, String message) {
        if (arg == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
