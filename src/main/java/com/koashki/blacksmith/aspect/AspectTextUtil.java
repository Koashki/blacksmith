package com.koashki.blacksmith.aspect;

import net.minecraft.resources.ResourceLocation;

public final class AspectTextUtil {
    private AspectTextUtil() {}

    public static String prettify(ResourceLocation id) {
        String[] parts = id.getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append(' ');
                }
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
