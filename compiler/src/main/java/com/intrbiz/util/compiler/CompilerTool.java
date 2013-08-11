package com.intrbiz.util.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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
 * A rather poor and primitive wrapper to the JDK (6) Tools API.
 * Making it simple to compile and load a Java class at Runtime.
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
        this.fileManager = this.compiler.getStandardFileManager(null, null, null);
        //
        try
        {
            this.base = Files.createTempDirectory("intrbiz-rt-classes-").toFile();
            logger.trace("Using " + this.base + " as compilation directory");
            // set the output directory
            this.fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File[]{ this.base }));
            // append our output directory to the compiler class path
            List<File> classPath = new LinkedList<File>();
            for (File file : this.fileManager.getLocation(StandardLocation.CLASS_PATH))
            {
                classPath.add(file);
            }
            classPath.add(this.base);
            this.fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
        }
        catch (IOException e)
        {
            logger.error("Failed to setup temporary compilation directory", e);
        }
        //
        this.loader = new SimpleClassLoader(ClassLoader.getSystemClassLoader(), this.fileManager);
    }
    
    public ClassLoader getLoader()
    {
        return this.loader;
    }
    
    public synchronized boolean compileClass(String className, String classContent)
    {
        if (logger.isTraceEnabled())
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
        return this.compiler.getTask(null, this.fileManager, null, null, null, Arrays.asList(new JavaFileObject[]{ new MemoryJavaFile(className, classContent) })).call();
    }
    
    public synchronized Class<?> defineClass(String className, String classContent) throws ClassNotFoundException
    {
        if (this.compileClass(className, classContent))
        {
            return this.loader.loadClass(className);
        }
        return null;
    }
    
    public synchronized Class<?> defineClass(JavaClass jCls) throws ClassNotFoundException
    {
        return this.defineClass(jCls.getCanonicalName(), jCls.toJava(""));
    }
}
