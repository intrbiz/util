package com.intrbiz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Package an application
 * 
 * @goal intrbiz-package
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution runtime
 */
public class IntrbizPackaging extends AbstractMojo
{
    private static Set<String> BOOTSTRAP_CLASSES = new HashSet<String>();

    static
    {
        BOOTSTRAP_CLASSES.add("com/intrbiz/util/IntrbizBootstrap.class");
    }

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
     * @parameter
     * @readonly
     */
    private String appClass;

    public IntrbizPackaging()
    {
        super();
    }

    public void execute() throws MojoExecutionException
    {
        Log log = this.getLog();
        log.info("Packaging application");
        // the output file
        File app = new File(this.targetDirectory, this.artifactId + "-" + this.version + ".app");
        log.info("  Writing: " + app.getAbsolutePath());
        // open the output file
        JarOutputStream out = null;
        try
        {
            try
            {
                out = new JarOutputStream(new FileOutputStream(app), this.createManifest());
                // add in the classes
                for (File cls : this.scanDirectory(this.classesDirectory, ".class"))
                {
                    this.putFile(out, "classes/", this.classesDirectory, cls);
                }
                // resources
                File resDir = new File(this.baseDirectory, "src/main/resources");
                if (resDir.exists())
                {
                    for (File res : this.scanDirectory(resDir))
                    {
                        this.putFile(out, "classes/", resDir, res);
                    }
                }
                // add libraries
                for (File lib : this.collectDependencies())
                {
                    this.putFile(out, "lib/", lib);
                }
                // add public resources
                File publicDir = new File(this.baseDirectory, "src/main/public");
                for (File pub : this.scanDirectory(publicDir))
                {
                    this.putFile(out, "public/", publicDir, pub);
                }
                // add views
                File viewsDir = new File(this.baseDirectory, "src/main/views");
                for (File view : this.scanDirectory(viewsDir))
                {
                    this.putFile(out, "views/", viewsDir, view);
                }
                // add cfg
                File cfgDir = new File(this.baseDirectory, "src/main/cfg");
                if (cfgDir.exists())
                {
                    for (File view : this.scanDirectory(cfgDir))
                    {
                        this.putFile(out, "cfg/", cfgDir, view);
                    }
                }
                // add bin
                File binDir = new File(this.baseDirectory, "src/main/bin");
                if (binDir.exists())
                {
                    for (File view : this.scanDirectory(binDir))
                    {
                        this.putFile(out, "bin/", binDir, view);
                    }
                }
                // copy in the bootstrap classes
                File bstrap = this.findBoostrapArtifact();
                if (bstrap == null) throw new MojoExecutionException("Unable to find bootstrap as a dependency");
                this.copyBootstrap(out, bstrap);
            }
            finally
            {
                if (out != null) out.close();
            }
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Failed to write JAR file", e);
        }
    }

    protected void copyBootstrap(JarOutputStream out, File from) throws IOException
    {
        Log log = this.getLog();
        JarInputStream in = null;
        try
        {
            in = new JarInputStream(new FileInputStream(from));
            JarEntry je;
            while ((je = in.getNextJarEntry()) != null)
            {
                if (BOOTSTRAP_CLASSES.contains(je.getName()))
                {
                    log.debug("Copying class: " + je.getName());
                    out.putNextEntry(new ZipEntry(je.getName()));
                    this.copyStream(out, in);
                }
            }
        }
        finally
        {
            if (in != null) in.close();
        }
    }

    protected void putFile(JarOutputStream out, String path, File base, File file) throws IOException
    {
        String filePath = path + file.getAbsolutePath().substring(base.getAbsolutePath().length() + 1).replace('\\', '/');
        this.getLog().debug("  Adding file: " + filePath);
        this.putFile(out, new ZipEntry(filePath), file);
    }

    protected void putFile(JarOutputStream out, String path, File file) throws IOException
    {
        String filePath = path + file.getName();
        this.getLog().debug("  Adding file: " + filePath);
        this.putFile(out, new ZipEntry(filePath), file);
    }

    protected void putFile(JarOutputStream out, ZipEntry ze, File file) throws IOException
    {
        out.putNextEntry(ze);
        // copy the file in
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(file);
            this.copyStream(out, in);
        }
        finally
        {
            if (in != null) in.close();
        }
    }

    protected void copyStream(OutputStream to, InputStream from) throws IOException
    {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = from.read(buffer)) != -1)
        {
            to.write(buffer, 0, len);
        }
    }

    protected File findBoostrapArtifact()
    {
        for (Artifact a : this.artifacts)
        {
            this.getLog().debug("Artifact: " + a.getGroupId() + " :: " + a.getArtifactId());
            //
            if ("com.intrbiz.util".equals(a.getGroupId()) && "bootstrap".equals(a.getArtifactId()))
                return a.getFile();
        }
        return null;
    }

    protected Manifest createManifest()
    {
        Manifest mf = new Manifest();
        Attributes mainAttributes = mf.getMainAttributes();
        // general attributes
        mainAttributes.putValue("Manifest-Version", "1.0");
        mainAttributes.putValue("Created-By", "1.0.0 Intrbiz Packaging");
        // the main class
        mainAttributes.putValue("Main-Class", "com.intrbiz.util.IntrbizBootstrap");
        // the app class
        if (this.appClass != null)
        {
            mainAttributes.putValue("App-Class", this.appClass);
        }
        return mf;
    }

    protected Set<File> collectDependencies()
    {
        Set<File> s = new HashSet<File>();
        for (Artifact a : this.artifacts)
        {
            s.add(a.getFile());
        }
        return s;
    }

    protected Set<File> scanDirectory(File dir)
    {
        return this.scanDirectory(dir, null);
    }

    protected Set<File> scanDirectory(File dir, String extension)
    {
        Set<File> s = new HashSet<File>();
        Stack<File> q = new Stack<File>();
        q.push(dir);
        while (!q.isEmpty())
        {
            File f = q.pop();
            if (f.isFile())
            {
                if (extension == null || f.getName().endsWith(extension)) s.add(f);
            }
            else if (f.isDirectory())
            {
                for (File c : f.listFiles())
                {
                    q.add(c);
                }
            }
        }
        return s;
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

    public String getAppClass()
    {
        return appClass;
    }

    public void setAppClass(String appClass)
    {
        this.appClass = appClass;
    }
}
