/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akita.util;

import org.akita.annotation.AkTest;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * Date: 12-4-23
 * Time: 下午5:09
 *
 * @author zhe.yangz
 */
public class JunitUtil {

    /**
     * 得到AkTest标注的名称，若没有则返回null
     * @param testCase
     * @return
     */
    public static String getTestName(TestCase testCase) {
        try {
            Method method = testCase.getClass().getMethod(testCase.getName(), (Class[])null);
            if (method != null) {
                AkTest akTest = method.getAnnotation(AkTest.class);
                if (akTest != null) {
                    return akTest.value();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //defaults
            return null;
        }
    }
}
