package com.intrbiz.scripting;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

/**
 * <p>
 * Default permissions which a script is permitted in a restricted context
 * </p>
 * <p>
 * These set of permissions don't allow:
 * </p>
 * <ul>
 *     <li>System.exit() to be called</li>
 *     <li>Setting of system properties</li>
 *     <li>Any file access</li>
 *     <li>Opening network sockets</li>
 *     <li>Setting methods and fields accessible via reflection</li>
 * </ul>
 */
public class RestrictedScriptPermissions extends PermissionCollection
{
    private static final long serialVersionUID = 1L;

    private Permissions permissions = new Permissions();

    public RestrictedScriptPermissions()
    {
        // restrictive set of runtime permissions
        add(new RuntimePermission("getClassLoader"));
        add(new RuntimePermission("getenv.*"));
        add(new RuntimePermission("getProtectionDomain"));
        add(new RuntimePermission("getFileSystemAttributes"));
        add(new RuntimePermission("readFileDescriptor"));
        add(new RuntimePermission("accessClassInPackage.*"));
        add(new RuntimePermission("defineClassInPackage.*"));
        add(new RuntimePermission("accessDeclaredMembers"));
        add(new RuntimePermission("queuePrintJob"));
        add(new RuntimePermission("getStackTrace"));
        add(new RuntimePermission("preferences"));
        // allow read properties
        add(new PropertyPermission("*", "read"));
        // allow init of java.util logging
        add(new LoggingPermission("control", null));
    }
    
    public void add(Permission permission)
    {
    }

    public boolean implies(Permission permission)
    {
        return permissions.implies(permission);
    }

    public Enumeration<Permission> elements()
    {
        return permissions.elements();
    }

    public boolean isReadOnly()
    {
        return true;
    }

    public void setReadOnly()
    {
    }
}
