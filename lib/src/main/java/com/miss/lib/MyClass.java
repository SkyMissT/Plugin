package com.miss.lib;

import java.lang.reflect.Field;

public class MyClass {

    public static void main(String[] args) {

        Man man = new Man(13);

        try {
            Class<?> clazz = Class.forName("com.miss.lib.Man");

            Field field = clazz.getDeclaredField("age");
            field.setAccessible(true);

            int change = (int) field.get(man);

            System.out.println("change = " + change);

            field.set(man, 18);

            System.out.println("change = " + man.getAge());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}