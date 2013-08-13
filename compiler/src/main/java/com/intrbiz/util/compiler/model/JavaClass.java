package com.intrbiz.util.compiler.model;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.intrbiz.util.compiler.util.JavaUtil;

/**
 * A simple model of a Java Class to make generating code easier
 */
public class JavaClass
{
    private String packageName;

    private Set<String> imports = new TreeSet<String>();

    private String name;

    private String superClass;

    private Set<String> superInterfaces = new TreeSet<String>();

    private List<JavaField> fields = new LinkedList<JavaField>();

    private List<JavaConstructor> constructors = new LinkedList<JavaConstructor>();

    private List<JavaMethod> methods = new LinkedList<JavaMethod>();

    public JavaClass()
    {
        super();
    }

    public JavaClass(String packageName, String name)
    {
        this();
        this.packageName = packageName;
        this.name = name;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public JavaClass setPackageName(String packageName)
    {
        this.packageName = packageName;
        return this;
    }

    public Set<String> getImports()
    {
        return imports;
    }

    public void setImports(Set<String> imports)
    {
        this.imports = imports;
    }
    
    public JavaClass addImport(String name)
    {
        if (! JavaUtil.isJavaPrimitive(name)) this.imports.add(name);
        return this;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getCanonicalName()
    {
        return this.getPackageName() + "." + this.getName();
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public JavaClass setSuperClass(String superClass)
    {
        this.superClass = superClass;
        return this;
    }

    public Set<String> getSuperInterfaces()
    {
        return superInterfaces;
    }

    public void setSuperInterfaces(Set<String> superInterfaces)
    {
        this.superInterfaces = superInterfaces;
    }
    
    public void addSuperInterface(String superInterface)
    {
        this.superInterfaces.add(superInterface);
    }

    public List<JavaField> getFields()
    {
        return fields;
    }

    public void setFields(List<JavaField> fields)
    {
        this.fields = fields;
    }
    
    public JavaField findField(String name)
    {
        for (JavaField field : this.fields)
        {
            if (name.equals(field.getName()))
                return field;
        }
        return null;
    }
    
    public JavaField addField(JavaField field)
    {
        this.fields.add(field);
        field.setJavaClass(this);
        return field;
    }
    
    public JavaField newField(String type, String name)
    {
        return this.addField(new JavaField(this, type, name));
    }
    
    public JavaField newUniqueField(String type, String name)
    {
        String uname = name;
        int i = 0;
        while (this.findField(uname) != null)
        {
            uname = name + (i++);
        }
        return this.newField(type, uname);
    }

    public List<JavaConstructor> getConstructors()
    {
        return constructors;
    }

    public void setConstructors(List<JavaConstructor> constructors)
    {
        this.constructors = constructors;
    }
    
    public JavaConstructor newConstructor(JavaParameter... parameters)
    {
        JavaConstructor c = new JavaConstructor(this);
        this.constructors.add(c);
        for (JavaParameter p : parameters)
        {
            c.addParameter(p);
        }
        return c;
    }

    public List<JavaMethod> getMethods()
    {
        return methods;
    }

    public void setMethods(List<JavaMethod> methods)
    {
        this.methods = methods;
    }
    
    public JavaMethod newMethod(String returnType, String name, JavaParameter... parameters)
    {
        JavaMethod m = new JavaMethod(this, name);
        this.methods.add(m);
        m.setReturnType(returnType);
        for (JavaParameter p : parameters)
        {
            m.addParameter(p);
        }
        return m;
    }
    
    public JavaMethod addMethod(JavaMethod method)
    {
        this.methods.add(method);
        method.setJavaClass(this);
        return method;
    }
    
    public JavaMethod newMethod(Method prototype)
    {
        JavaMethod m = new JavaMethod();
        this.addMethod(m);
        // name
        m.setName(prototype.getName());
        // return type;
        if (void.class != prototype.getReturnType())
        {
            this.addImport(prototype.getReturnType().getCanonicalName());
            m.setReturnType(prototype.getReturnType().getSimpleName());
        }
        // parameters
        int idx = 0;
        for (Class<?> pType : prototype.getParameterTypes())
        {
            this.addImport(pType.getCanonicalName());
            m.addParameter(new JavaParameter(pType.getSimpleName(), "p" + (idx++)));
        }
        return m;
    }
    
    
    public String toJava(String p)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("package ").append(this.getPackageName()).append(";\r\n");
        sb.append("\r\n");
        for (String imp : this.getImports())
        {
            sb.append("import ").append(imp).append(";\r\n");
        }
        sb.append("\r\n");
        sb.append("@SuppressWarnings(\"all\")\r\n");
        sb.append("public class ").append(this.getName());
        if (this.getSuperClass() != null)
        {
            sb.append(" extends ").append(this.getSuperClass());
        }
        if (!this.getSuperInterfaces().isEmpty())
        {
            sb.append(" implements ");
            boolean ns = false;
            for (String si : this.getSuperInterfaces())
            {
                if (ns) sb.append(", ");
                sb.append(si);
                ns = true;
            }
        }
        sb.append("\r\n");
        sb.append("{\r\n");
        sb.append("\r\n");
        for (JavaField f : this.getFields())
        {
            sb.append(f.toJava(p + "\t"));
            sb.append("\r\n");
        }
        sb.append("\r\n");
        for (JavaConstructor c : this.getConstructors())
        {
            sb.append(c.toJava(p + "\t"));
            sb.append("\r\n");
        }
        sb.append("\r\n");
        for (JavaMethod m : this.getMethods())
        {
            sb.append(m.toJava(p + "\t"));
            sb.append("\r\n");
        }
        sb.append("\r\n");
        sb.append("}\r\n");
        sb.append("\r\n");
        return sb.toString();
    }
}
