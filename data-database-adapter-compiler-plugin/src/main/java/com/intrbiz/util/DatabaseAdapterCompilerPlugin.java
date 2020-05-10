package com.intrbiz.util;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Compile database adapters at compile time rather than runtime
 * 
 * @goal intrbiz-data-adapter
 * @phase process-classes
 * @requiresProject
 * @requiresDependencyResolution runtime
 */
public class DatabaseAdapterCompilerPlugin extends AbstractMojo
{
    /**
     * @parameter expression="${project.basedir}"
     * @required
     */
    private File baseDirectory;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String artifactId;

    /**
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

    /**
     * @parameter default-value="${project.artifacts}"
     * @required
     * @readonly
     */
    private Collection<Artifact> artifacts;
    
    /**
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> compilePath;

    /**
     * @parameter
     * @required
     * @readonly
     */
    private String adapterClass;
    
    /**
     * @parameter
     * @required
     * @readonly
     */
    private String defaultOwner;

    public DatabaseAdapterCompilerPlugin()
    {
        super();
    }

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException
    {
        Log log = this.getLog();
        try
        {
            log.info("Compiling database adapter: " + this.getAdapterClass());
            // setup our class loader
            Set<URL> urls = new HashSet<URL>();
            for (String ce : this.getCompilePath())
            {
                log.info("Adding compile path element: " + ce);
                urls.add(new File(ce).toURI().toURL());
            }
            // set the compiler target directory
            System.getProperties().setProperty("intrbiz.runtime.target", this.getClassesDirectory().getAbsolutePath());
            System.getProperties().setProperty("com.intrbiz.compiler.source", "true");
            // invoke the compiler
            try (URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[0])))
            {
                // load the compiler, we need to use reflect to do the invocations
                Class<?> compilerClass = ucl.loadClass("com.intrbiz.data.db.compiler.DatabaseAdapterCompiler");
                Method compilerFactory = compilerClass.getMethod("defaultPGSQLCompiler", new Class<?>[] { String.class });
                Method implCompiler = compilerClass.getMethod("compileAdapterImplementation", new Class<?>[] { Class.class });
                Method factCompiler = compilerClass.getMethod("compileAdapterFactory", new Class<?>[] { Class.class });
                Method sqlInstallCompiler = compilerClass.getMethod("compileInstallSchemaToString", new Class<?>[] { Class.class });
                Method sqlUpgradeCompiler = compilerClass.getMethod("compileAllUpgradeSchemasToString", new Class<?>[] { Class.class });
                // create our compiler instance
                Object compiler = compilerFactory.invoke(null, new Object[] { this.getDefaultOwner() });
                log.info("Created compiler: " + compiler);
                // load the adapter definition
                Class<?> adapterDefinition = ucl.loadClass(this.getAdapterClass());
                log.info("Got adapter definition: " + adapterDefinition);
                // compile the class
                Class<?> compiledAdapter = (Class<?>) implCompiler.invoke(compiler, new Object[] { adapterDefinition });
                log.info("Compiled adapter implementation: " + compiledAdapter.getCanonicalName());
                // compile the factory
                Object factory = factCompiler.invoke(compiler, new Object[] { adapterDefinition });
                log.info("Compiled adapter factory: " + factory.getClass().getCanonicalName());
                // build the SQL and write to a file in the target dir
                String installSql = (String) sqlInstallCompiler.invoke(compiler, new Object[] { adapterDefinition });
                File installFile = new File(this.getTargetDirectory(), "install.sql");
                try (FileWriter fw = new FileWriter(installFile))
                {
                    fw.write(installSql);
                }
                log.info("Wrote install SQL to " + installFile.getAbsolutePath());
                // build the SQL upgrade files
                Map<String, String> upgradeSqls = (Map<String, String>) sqlUpgradeCompiler.invoke(compiler, new Object[] { adapterDefinition });
                for (Entry<String, String> upgradeSql : upgradeSqls.entrySet())
                {
                    File upgradeFile = new File(this.getTargetDirectory(), "upgrade-from-" + upgradeSql.getKey() + ".sql");
                    try (FileWriter fw = new FileWriter(upgradeFile))
                    {
                        fw.write(upgradeSql.getValue());
                    }
                    log.info("Wrote upgrade SQL to " + upgradeFile.getAbsolutePath());
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error compiling database adapter", e);
            throw new MojoExecutionException("Failed to compile database adapter", e);
        }
    }

    public File getBaseDirectory()
    {
        return baseDirectory;
    }

    public void setBaseDirectory(File baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }

    public File getClassesDirectory()
    {
        return classesDirectory;
    }

    public void setClassesDirectory(File classesDirectory)
    {
        this.classesDirectory = classesDirectory;
    }

    public File getTargetDirectory()
    {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory)
    {
        this.targetDirectory = targetDirectory;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Collection<Artifact> getArtifacts()
    {
        return artifacts;
    }

    public void setArtifacts(Collection<Artifact> artifacts)
    {
        this.artifacts = artifacts;
    }

    public String getAdapterClass()
    {
        return adapterClass;
    }

    public void setAdapterClass(String adapterClass)
    {
        this.adapterClass = adapterClass;
    }

    public List<String> getCompilePath()
    {
        return compilePath;
    }

    public void setCompilePath(List<String> compilePath)
    {
        this.compilePath = compilePath;
    }

    public String getDefaultOwner()
    {
        return defaultOwner;
    }

    public void setDefaultOwner(String defaultOwner)
    {
        this.defaultOwner = defaultOwner;
    }
}
