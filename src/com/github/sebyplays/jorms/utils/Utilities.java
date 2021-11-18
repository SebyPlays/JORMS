package com.github.sebyplays.jorms.utils;

import com.github.sebyplays.jorms.utils.annotations.Column;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Utilities {

    public static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList classes = new ArrayList();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return (Class[]) classes.toArray(new Class[classes.size()]);
    }

    public static List findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists())
            return classes;
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static Annotation getAnnotation(Class clazz, Class annotation){
        if(clazz.isAnnotationPresent(annotation))
            return clazz.getAnnotation(annotation);
        return null;
    }

    @SneakyThrows
    public static void setFieldValue(Class clazz, String fieldName, Object fieldValue){
        Field field  = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(clazz, fieldValue);
    }

    @SneakyThrows
    public static Object getFieldValue(Class clazz, String fieldName){
        Field field  = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(clazz);
    }


    @SneakyThrows
    public static Object getFieldValue(Field field, Object o){
        field.setAccessible(true);
        return field.get(o);
    }

    public static String getNameOfColumn(Field field){
        return field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name().equals("{nameOfField}") ? field.getName() : field.getAnnotation(Column.class).name() : null;
    }
}
