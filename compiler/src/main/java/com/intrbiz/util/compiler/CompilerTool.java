package com.intrbiz.util.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;

import com.intrbiz.util.compiler.model.JavaClass;

/**
 * A rather poor and primitive wrapper to the JDK (6) Tools API. Making it simple to compile and load a Java class at Runtime.
 */
public final class CompilerTool
{
    private static final CompilerTool TOOL = new CompilerTool();

    public static final CompilerTool getInstance()
    {
        return TOOL;
    }

    private JavaCompiler compiler;

    private StandardJavaFileManager fileManager;

    private ClassLoader loader;

    private File base;

    private Logger logger = Logger.getLogger(CompilerTool.class);

    private CompilerTool()
    {
        super();
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) throw new RuntimeException("Failed to initialise the Java Tools compiler, check that the JDK is installed.");
        this.fileManager = this.compiler.getStandardFileManager(null, null, null);
        //
        try
        {
            this.base = this.getCompilationDir();
            logger.trace("Using " + this.base + " as compilation directory");
            // set the output directory
            this.fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File[] { this.base }));
            // append our output directory to the compiler class path
            this.appendClassPath(this.base);
            // look at our class loader
            ClassLoader ourLoader = CompilerTool.class.getClassLoader();
            if (ourLoader instanceof URLClassLoader)
            {
                for (URL url : ((URLClassLoader) ourLoader).getURLs())
                {
                    try
                    {
                        Path path = Paths.get(url.toURI());
                        logger.trace("Adding classpath entry: " + path.toFile().getAbsolutePath());
                        this.appendClassPath(path.toFile());
                    }
                    catch (URISyntaxException e)
                    {
                        logger.error("Failed to convert URL to file");
                    }
                }
            }
            // create a class loader for our compiled code chained with the class loader which loaded us
            this.loader = new SimpleClassLoader(ourLoader, this.fileManager);
        }
        catch (IOException e)
        {
            logger.error("Failed to setup temporary compilation directory", e);
        }
    }
    
    private File getCompilationDir() throws IOException
    {
        // look for a configured compilation directory
        String runtimeTarget = System.getProperty("intrbiz.runtime.target");
        if (runtimeTarget != null && runtimeTarget.length() > 0)
        {
            File target = new File(runtimeTarget);
            if (target.exists() && target.isDirectory())
                return target;
            logger.warn("The given compilation directory (intrbiz.runtime.target) does not exist or is not a directory, using default");
        }
        // default to a renerated temp path
        return Files.createTempDirectory("intrbiz-rt-classes-" + System.currentTimeMillis() + "-").toFile();
    }

    public void appendClassPath(File... paths)
    {
        try
        {
            List<File> classPath = new LinkedList<File>();
            for (File file : this.fileManager.getLocation(StandardLocation.CLASS_PATH))
            {
                classPath.add(file);
            }
            for (File path : paths)
            {
                classPath.add(path);
            }
            this.fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
        }
        catch (IOException e)
        {
            logger.error("Failed to append to the class path", e);
        }
    }

    public ClassLoader getLoader()
    {
        return this.loader;
    }

    public synchronized boolean compileClass(String className, String classContent)
    {
        if (logger.isTraceEnabled() || Boolean.getBoolean("com.intrbiz.compiler.source"))
        {
            logger.trace("Compiling Class: " + className);
            //
            File f = new File(this.base, className.replace('.', '/') + ".java");
            f.getParentFile().mkdirs();
            try
            {
                try (FileWriter fw = new FileWriter(f))
                {
                    fw.write(classContent);
                    fw.flush();
                }
            }
            catch (Exception e)
            {
            }
        }
        return this.compiler.getTask(null, this.fileManager, null, null, null, Arrays.asList(new JavaFileObject[] { new MemoryJavaFile(className, classContent) })).call();
    }

    public synchronized Class<?> defineClass(String className, String classContent) throws ClassNotFoundException
    {
        if (this.compileClass(className, classContent)) { return this.loader.loadClass(className); }
        return null;
    }

    public synchronized Class<?> defineClass(JavaClass jCls) throws ClassNotFoundException
    {
        return this.defineClass(jCls.getCanonicalName(), jCls.toJava(""));
    }
}
