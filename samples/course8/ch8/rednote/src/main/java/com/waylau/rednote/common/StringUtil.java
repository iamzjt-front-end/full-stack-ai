package com.waylau.rednote.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * StringUtil 字符串工具类
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/18
 **/
public class StringUtil {
    // 字符串转为List
    public static List<String> splitToList(String source, String regex) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(source.split(regex));
    }
}
