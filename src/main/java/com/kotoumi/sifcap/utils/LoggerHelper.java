package com.kotoumi.sifcap.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guohaohao
 */
public class LoggerHelper {

    private static final Logger loggerInput = LoggerFactory.getLogger("Input");

    /**
     * input log
     *
     * @param input input
     * @return
     */
    public static void logInput(String input) {
        loggerInput.info(input);
    }

}
