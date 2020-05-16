package com.intrbiz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Bootstrap an application from a single jar file, created by the intrbiz-packaging plugin
 */
public final class IntrbizBootstrap
{
    public static final String APP_CLASS_ATTRIBUTE_NAME = "App-Class";
    
    private static boolean DEBUG = getBoolProp("bootstrap.debug", false);
    
    private static boolean LOADED = false;
    
    private static File APP_FILE;
    
    private static File APP_DIR;
    
    private static final Map<String, File> PLUGIN_APP_FILES = new HashMap<String, File>();
   
    private static final Map<String, File> PLUGIN_DIRS = new HashMap<String, File>();
    
    private static ClassLoader ROOT;
    
    private static URLClassLoader APP_LOADER;
    
    private static final Map<String, URLClassLoader> PLUGIN_LOADERS = new HashMap<String, URLClassLoader>();
    
    private static Map<String, String> APP_ATTRIBUTES;
    
    private static final Map<String, Map<String, String>> PLUGIN_ATTRIBUTES = new HashMap<String, Map<String, String>>();
    
    private static String APP_CLASS_NAME;
    
    // Information exposed
    
    public static boolean isBootstrapped()
    {
        return LOADED;
    }
    
    public static File getApplicationDirectory()
    {
        return APP_DIR;
    }
    
    public static URLClassLoader getApplicationClassLoader()
    {
        return APP_LOADER;
    }
    
    public static Map<String, String> getApplicationAttributes()
    {
        return APP_ATTRIBUTES == null ? new HashMap<String, String>() : Collections.unmodifiableMap(APP_ATTRIBUTES);
    }
    
    public static Set<String> getPluginNames()
    {
        return Collections.unmodifiableSet(PLUGIN_DIRS.keySet());
    }
    
    public static Collection<URLClassLoader> getPluginClassLoaders()
    {
        return Collections.unmodifiableCollection(PLUGIN_LOADERS.values());
    }
    
    public static URLClassLoader getPluginClassLoader(String pluginName)
    {
        return PLUGIN_LOADERS.get(pluginName);
    }
    
    public static File getPluginDirectory(String pluginName)
    {
        return PLUGIN_DIRS.get(pluginName);
    }
    
    public static Map<String, String> getPluginAttributes(String pluginName)
    {
        Map<String, String> attrs = PLUGIN_ATTRIBUTES.get(pluginName);
        return attrs == null ? new HashMap<String, String>() : Collections.unmodifiableMap(attrs);
    }
    
    public static void main(String[] args) throws Exception
    {
        log("Intrbiz Bootstrap Starting");
        // get our jar
        APP_FILE = findAppJar();
        // our application dir
        APP_DIR = APP_FILE.getAbsoluteFile().getParentFile();
        log("Using main application directory: " + APP_DIR.getAbsolutePath());
        // load application attributes from the jar manifest
        APP_ATTRIBUTES = getAppAttributes(APP_FILE);
        log("Loaded application attributes: " + APP_ATTRIBUTES);
        // optionally extract
        if (getBoolProp("bootstrap.extract", true))
        {
            log("Extracting main application");
            extractApp(APP_FILE, APP_DIR);
        }
        // find and extract plugins
        for (File pluginAppFile : findPluginAppJars())
        {
            String pluginName = pluginAppFile.getName().substring(0, pluginAppFile.getName().length() - 4);
            File pluginDir = new File(pluginAppFile.getParentFile(), pluginName);
            // extract the plugin
            log("Extracting plugin application: " + pluginName);
            extractApp(pluginAppFile, pluginDir);
            // load the plugin attributes
            Map<String, String> pluginAttrs = getAppAttributes(pluginAppFile);
            // store the plugin
            PLUGIN_APP_FILES.put(pluginName, pluginAppFile);
            PLUGIN_DIRS.put(pluginName, pluginDir);
            PLUGIN_ATTRIBUTES.put(pluginName, pluginAttrs);
        }
        // only extract do not run
        if (getBoolProp("bootstrap.extract.only", false))
        {
            log("Extracted application and plugins");
            System.exit(0);
        }
        // create the class loaders
        ROOT = IntrbizBootstrap.class.getClassLoader();
        APP_LOADER = createClassLoader(APP_DIR, ROOT);
        for (Entry<String, File> plugin : PLUGIN_DIRS.entrySet())
        {
            PLUGIN_LOADERS.put(plugin.getKey(), createClassLoader(plugin.getValue(), APP_LOADER));
        }
        // set the context class loader
        Thread.currentThread().setContextClassLoader(APP_LOADER);
        // get the app class
        LOADED = true;
        APP_CLASS_NAME = APP_ATTRIBUTES.get(APP_CLASS_ATTRIBUTE_NAME);
        if (APP_CLASS_NAME != null)
        {
            // load the app class
            Class<?> appClass = APP_LOADER.loadClass(APP_CLASS_NAME);
            if (appClass == null)
            {
                log("Failed to load class: " + APP_CLASS_NAME);
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
    }
    
    private static List<File> findPluginAppJars()
    {
        List<File> pluginAppJars = new LinkedList<File>();
        String plugins = getProp("bootstrap.plugins", null);
        if (plugins != null)
        {
            for (String path : plugins.split(":"))
            {
                path = path.trim();
                if (path.length() > 0)
                {
                    File pluginFile = new File(path);
                    if (pluginFile.exists())
                    {
                        if (pluginFile.isFile())
                        {
                            if (pluginFile.getName().endsWith(".app"))
                            {
                                pluginAppJars.add(pluginFile);
                            }
                        }
                        else if (pluginFile.isDirectory())
                        {
                            File[] files = pluginFile.listFiles();
                            if (files != null)
                            {
                                for (File file : files)
                                {
                                    if (file.isFile() && file.getName().endsWith(".app"))
                                    {
                                        pluginAppJars.add(file);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pluginAppJars;
    }
    
    private static URLClassLoader createClassLoader(File workingDir, ClassLoader parent) throws MalformedURLException
    {
        // build the classpath
        Set<URL> urls = new HashSet<URL>();
        log("Building classpath for " + workingDir.getName());
        // classes
        File classesDir = new File(workingDir, "classes");
        if (classesDir.exists() && classesDir.isDirectory())
        {
            urls.add(classesDir.toURI().toURL());
        }
        // libraries
        File libsDir = new File(workingDir, "lib");
        if (libsDir.exists() && libsDir.isDirectory())
        {
            File[] libs = libsDir.listFiles();
            if (libs != null)
            {
                for (File lib : libs)
                {
                    if (lib.getName().endsWith(".jar"))
                    {
                        urls.add(lib.toURI().toURL());
                    }
                }
            }
        }
        if (urls.isEmpty())
        {
            throw new RuntimeException("Failed to find any classes to load, is the directory correct: " + workingDir);
        }
        return URLClassLoader.newInstance(urls.toArray(new URL[0]), parent);
    }
    
    private static File findAppJar() throws IOException, URISyntaxException
    {
        URL mainApp = IntrbizBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        if (mainApp == null) throw new RuntimeException("Failed to find main application JAR location");
        File mainAppFile = new File(mainApp.toURI());
        log("Using main application Jar: " + mainApp + " file=" + mainAppFile.getAbsolutePath() + " " + mainAppFile.exists());
        if (! mainAppFile.exists()) throw new RuntimeException("Failed to get JAR file, name=" + mainAppFile.getAbsolutePath() + ", exists=" + mainAppFile.exists());
        return mainAppFile;
    }
    
    private static Map<String, String> getAppAttributes(File jar) throws IOException
    {
        Map<String, String> attrs = new HashMap<String, String>();
        JarInputStream in = new JarInputStream(new FileInputStream(jar));
        try
        {
            Manifest mf = in.getManifest();
            Attributes main = mf.getMainAttributes();
            for (Entry<Object, Object> entry : main.entrySet())
            {
                attrs.put(String.valueOf(entry.getKey()), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
            }
        }
        finally
        {
            in.close();
        }
        return attrs;
    }
    
    private static void extractApp(File jarFile, File toPath) throws IOException
    {
        // ensure the containing directory exists
        if (! toPath.exists())
        {
            toPath.mkdirs();
        }
        // extract the app jar
        JarInputStream in = new JarInputStream(new FileInputStream(jarFile));
        try
        {
            JarEntry je;
            while ((je = in.getNextJarEntry()) != null)
            {
                String name = je.getName();
                if (name.startsWith("classes/") || name.startsWith("lib/") || name.startsWith("public/") || name.startsWith("views/") || name.startsWith("cfg/") || name.startsWith("bin/"))
                {
                    File file = new File(toPath, name);
                    file.getParentFile().mkdirs();
                    // write
                    FileOutputStream out = new FileOutputStream(file);
                    try
                    {
                        copy(out, in);
                    }
                    finally
                    {
                        out.close();
                    }
                }
            }
        }
        finally
        {
            in.close();
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
    
    private static String getProp(String name, String defaultValue)
    {
        String env = System.getenv(name.replace('.', '_').replace('-', '_').toUpperCase());
        if (env != null && env.length() > 0) return env;
        env = System.getProperty(name);
        if (env != null && env.length() > 0) return env;
        return defaultValue;
    }
    
    private static boolean getBoolProp(String name, boolean defaultValue)
    {
        String val = getProp(name, null);
        return val != null ? Boolean.parseBoolean(val) : defaultValue;
    }
    
    private static void log(String message)
    {
        if (DEBUG) System.out.println(message);
    }
}
