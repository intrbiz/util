package com.intrbiz.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Bootstrap an application from a single jar file, created by the intrbiz-packaging plugin
 */
public class IntrbizBootstrap
{
    public static boolean DEBUG = Boolean.getBoolean("bootstrap.debug");
    
    public static void log(String message)
    {
        if (DEBUG) System.out.println(message);
    }
    
    public static void main(String[] args) throws Exception
    {
        log("Intrbiz Bootstrap Starting");
        // get our jar
        URL jar = findJar();
        if (jar == null) throw new RuntimeException("Failed to find JAR location");
        log("Using Jar: " + jar);
        // our working dir
        File workingDir = (new File(".")).getAbsoluteFile().getParentFile();
        log("Using working directory: " + workingDir.getAbsolutePath());
        // optionally extract
        if (Boolean.parseBoolean(System.getProperty("bootstrap.extract", "true")))
        {
            log("Extracting application");
            extractJar(jar, workingDir);
            // only extract do not run
            if (Boolean.parseBoolean(System.getProperty("bootstrap.extract.only", "false")))
            {
                log("Extracted application");
                System.exit(0);
            }
        }
        // create a class loader
        URLClassLoader classLoader = createClassLoader(workingDir);
        // set the context class loader
        Thread.currentThread().setContextClassLoader(classLoader);
        // get the app class
        String appClassName = getAppClass(jar);
        // load the app class
        Class<?> appClass = classLoader.loadClass(appClassName);
        if (appClass == null)
        {
            log("Failed to load class: " + appClassName);
            System.exit(-1);
        }
        Method main = appClass.getDeclaredMethod("main", String[].class);
        if (main == null || (! Modifier.isStatic(main.getModifiers())))
        {
            log("Could not find main method on class: " + appClass);
            System.exit(-1);
        }
        main.invoke(null, new Object[] { args });
    }
    
    private static URLClassLoader createClassLoader(File workingDir) throws MalformedURLException
    {
        Set<URL> urls = new HashSet<URL>();
        // classes
        urls.add((new File(workingDir, "classes")).toURI().toURL());
        // lib
        for (File lib : (new File(workingDir, "lib")).listFiles())
        {
            if (lib.getName().endsWith(".jar"))
            {
                urls.add(lib.toURI().toURL());
            }
        }
        return URLClassLoader.newInstance(urls.toArray(new URL[0]));
    }
    
    private static URL findJar() throws IOException
    {
        return IntrbizBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
    }
    
    private static String getAppClass(URL jar) throws IOException
    {
        try (JarInputStream in = new JarInputStream(jar.openStream()))
        {
            Manifest mf = in.getManifest();
            Attributes main = mf.getMainAttributes();
            return main.getValue("App-Class");
        }
    }
    
    private static void extractJar(URL jar, File working) throws IOException
    {
        try (JarInputStream in = new JarInputStream(jar.openStream()))
        {
            JarEntry je;
            while ((je = in.getNextJarEntry()) != null)
            {
                String name = je.getName();
                if (name.startsWith("classes/") || name.startsWith("lib/") || name.startsWith("public/") || name.startsWith("views/") || name.startsWith("cfg/") || name.startsWith("bin/"))
                {
                    File file = new File(working, name);
                    file.getParentFile().mkdirs();
                    try (FileOutputStream out = new FileOutputStream(file))
                    {
                        copy(out, in);
                    }
                }
            }
        }
    }
    
    private static void copy(OutputStream out, InputStream in) throws IOException
    {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, len);
        }
    }
}
