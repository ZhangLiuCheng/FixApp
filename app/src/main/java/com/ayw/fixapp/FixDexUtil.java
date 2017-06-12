package com.ayw.fixapp;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class FixDexUtil {

    public static String DEX_DIR = "dex";

    public static void fix(Context context) {
        List<File> dexFiles = new ArrayList<>();
        File fileDir = context.getDir(DEX_DIR, Context.MODE_PRIVATE);
        File[] listFile = fileDir.listFiles();
        for (File file : listFile) {
            if (file.getName().startsWith("patch") && file.getName().endsWith("dex")) {
                dexFiles.add(file);
            }
        }
        doDexInject(context, fileDir, dexFiles);
    }

    public static void doDexInject(Context context, File fileDir, List<File> dexs) {
        String optDir = fileDir.getParentFile().getAbsolutePath() + File.separator + "opt_dir";
        File optFile = new File(optDir);
        if (!optFile.exists()) {
            optFile.mkdir();
        }
        try {
            for (File dx : dexs) {
                DexClassLoader dexClassLoader = new DexClassLoader(dx.getAbsolutePath(),
                        optFile.getAbsolutePath(),
                        null,
                        context.getClassLoader());
                inject(context, dexClassLoader);
            }
        } catch (Exception e) {

        }
    }

    public static void inject(Context context, DexClassLoader dexClassLoader) {
        boolean hasBaseDexClassLoader = true;
        try {
            Class.forName("dalvik.system.BaseDexClassLoader");
        } catch (ClassNotFoundException e) {
            hasBaseDexClassLoader = false;
        }
        if (hasBaseDexClassLoader) {
            PathClassLoader pathClassLoader = (PathClassLoader)context.getClassLoader();
            try {
                Object dexElements = combineArray(getDexElements(getPathList(dexClassLoader)),
                        getDexElements(getPathList(pathClassLoader)));

                Object pathList = getPathList(pathClassLoader);
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
            } catch (Throwable e) {
            }
        }
    }

    public static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    public static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    public static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    public static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    public static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
}
