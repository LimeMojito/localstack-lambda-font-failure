/*
 * Copyright 2011-2025 Lime Mojito Pty Ltd
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.limemojito.fontfailure;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.awt.*;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, String> {
    public static final String HANDLER = Main.class.getName() + "::handleRequest";

    public static void main(String[] args) {
        dumpFontInfo();
    }

    @Override
    public String handleRequest(Map<String, Object> stringMap, Context context) {
        return dumpFontInfo();
    }

    public static String dumpFontInfo() {
        try {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            System.out.println("Available fonts:");
            final int fourKB = 4096;
            StringBuilder sb = new StringBuilder(fourKB);
            for (Font font : environment.getAllFonts()) {
                String info = font.getFontName() + " " + font.getFamily();
                sb.append(info).append("\n");
            }
            final String fontDump = sb.toString();
            System.out.println(fontDump);
            return fontDump;
        } catch (Throwable e) {
            return e.getClass().getName() + ": " + e.getMessage();
        }
    }
}
