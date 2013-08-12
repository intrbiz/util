package com.intrbiz.data.db.compiler.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.intrbiz.Util;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.introspector.function.GetterIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.RemoveIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.SQLFunctionIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.SetterIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLFunction;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSchema;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.ForeignKey;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.PrimaryKey;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.util.TextUtil;
import com.intrbiz.metadata.ListOf;

public class SQLIntrospector
{
    private Map<Class<? extends Annotation>, SQLFunctionIntrospector> functionIntrospectors = new IdentityHashMap<Class<? extends Annotation>, SQLFunctionIntrospector>();

    // caches
    
    private Map<Class<? extends DatabaseAdapter>, Schema> schemaCache = new IdentityHashMap<Class<? extends DatabaseAdapter>, Schema>();

    private Map<Class<?>, Table> tableCache = new IdentityHashMap<Class<?>, Table>();

    private Map<Class<?>, Type> typeCache = new IdentityHashMap<Class<?>, Type>();

    public SQLIntrospector()
    {
        super();
        // default SQL function introspectors
        this.registerFunctionIntrospector(SQLGetter.class, new GetterIntrospector());
        this.registerFunctionIntrospector(SQLSetter.class, new SetterIntrospector());
        this.registerFunctionIntrospector(SQLRemove.class, new RemoveIntrospector());
    }

    public void registerFunctionIntrospector(Class<? extends Annotation> type, SQLFunctionIntrospector introspector)
    {
        this.functionIntrospectors.put(type, introspector);
    }

    protected SQLFunctionIntrospector getFunctionIntrospector(Class<? extends Annotation> type)
    {
        return this.functionIntrospectors.get(type);
    }

    //

    public Schema buildSchema(SQLDialect dialect, Class<? extends DatabaseAdapter> cls)
    {
        Schema schema = this.schemaCache.get(cls);
        if (schema == null)
        {
            schema = new Schema(getSchemaName(cls), cls);
            this.schemaCache.put(cls, schema);
            schema.setVersion(getSchemaVersion(cls));
            this.buildTables(dialect, cls, schema);
            this.buildFunctions(dialect, cls, schema);
        }
        return schema;
    }

    protected void buildTables(SQLDialect dialect, Class<? extends DatabaseAdapter> cls, Schema schema)
    {
        SQLSchema tables = cls.getAnnotation(SQLSchema.class);
        if (tables != null)
        {
            for (Class<?> tCls : tables.tables())
            {
                Table table = this.buildTable(dialect, tCls, schema);
                schema.addTable(table);
                Type type = this.buildType(dialect, tCls, schema);
                schema.addType(type);
            }
        }
    }

    protected void buildFunctions(SQLDialect dialect, Class<? extends DatabaseAdapter> cls, Schema schema)
    {
        for (Method method : cls.getMethods())
        {
            Annotation sqlFunction = this.getFunctionAnnotation(method);
            if (sqlFunction != null)
            {
                // find an introspector
                SQLFunctionIntrospector introspector = this.getFunctionIntrospector(sqlFunction.annotationType());
                if (introspector != null)
                {
                    Function function = introspector.buildFunction(this, dialect, method, sqlFunction, cls, schema);
                    if (function != null) schema.addFunction(function);
                }
                else
                {
                    throw new RuntimeException("Could not get SQLFunctionIntrospector for annotation: " + sqlFunction.annotationType().getCanonicalName());
                }
            }
        }
    }

    protected Annotation getFunctionAnnotation(Method method)
    {
        for (Annotation ann : method.getAnnotations())
        {
            SQLFunction sfun = ann.annotationType().getAnnotation(SQLFunction.class);
            if (sfun != null) return ann;
        }
        return null;
    }

    public Type buildType(SQLDialect dialect, Class<?> cls, /* Nullable */Schema schema)
    {
        Type type = this.typeCache.get(cls);
        if (type == null)
        {
            Table table = this.buildTable(dialect, cls, schema);
            type = new Type("t_" + table.getName());
            this.typeCache.put(cls, type);
            type.setColumns(table.getColumns());
        }
        return type;
    }

    public Table buildTable(SQLDialect dialect, Class<?> cls, /* Nullable */Schema schema)
    {
        Table table = this.tableCache.get(cls);
        if (table == null)
        {
            table = new Table(schema, getTableName(cls), cls);
            this.tableCache.put(cls, table);
            // columns
            this.buildTable(dialect, cls, table, schema);
            // primary key
            table.setPrimaryKey(new PrimaryKey(table.getName() + "_pk"));
            this.buildPrimaryKey(dialect, cls, table);
            // foreign keys
            this.buildForeignKeys(dialect, cls, table);
        }
        return table;
    }

    protected void buildTable(SQLDialect dialect, Class<?> cls, Table model, /* Nullable */Schema schema)
    {
        if (cls == null) return;
        //
        buildTable(dialect, cls.getSuperclass(), model, schema);
        //
        List<Column> attrs = new LinkedList<Column>();
        for (Field field : cls.getDeclaredFields())
        {
            Column attr = buildColumn(dialect, field, cls);
            if (attr != null) attrs.add(attr);
        }
        Collections.sort(attrs);
        //
        for (Column attr : attrs)
        {
            model.addColumn(attr);
        }
    }

    protected Column buildColumn(SQLDialect dialect, Field field, Class<?> cls)
    {
        SQLColumn sa = field.getAnnotation(SQLColumn.class);
        if (sa != null)
        {
            String name = getColumnName(field);
            SQLType type = dialect.getType(field.getType());
            return new Column(sa.index(), name, type, field);
        }
        return null;
    }

    protected void buildPrimaryKey(SQLDialect dialect, Class<?> cls, Table table)
    {
        if (cls == null) return;
        this.buildPrimaryKey(dialect, cls.getSuperclass(), table);
        //
        for (Field field : cls.getDeclaredFields())
        {
            if (field.getAnnotation(SQLPrimaryKey.class) != null)
            {
                String colName = getColumnName(field);
                Column column = table.findColumn(colName);
                if (column != null)
                {
                    table.getPrimaryKey().addColumn(column);
                }
            }
        }
    }

    protected void buildForeignKeys(SQLDialect dialect, Class<?> cls, Table table)
    {
        if (cls == null) return;
        this.buildForeignKeys(dialect, cls.getSuperclass(), table);
        //
        for (Field field : cls.getDeclaredFields())
        {
            SQLForeignKey fkey = field.getAnnotation(SQLForeignKey.class);
            if (fkey != null)
            {
                ForeignKey key = new ForeignKey(Util.isEmpty(fkey.name()) ? TextUtil.camelCaseToUnderscores(field.getName()) + "_fk" : fkey.name());
                table.addForeignKey(key);
                // columns
                if (fkey.columns().length > 0)
                {
                    for (String colName : fkey.columns())
                    {
                        Column col = table.findColumn(colName);
                        if (col != null) key.addColumn(col);
                    }

                }
                else
                {
                    Column col = table.findColumn(getColumnName(field));
                    if (col != null) key.addColumn(col);
                }
                // references
                key.setReferences(this.buildTable(dialect, fkey.references(), null));
                // on
                for (String on : fkey.on())
                {
                    Column refCol = key.getReferences().findColumn(on);
                    if (refCol != null) key.addOn(refCol);
                }
                //
                key.setOnUpdate(fkey.onUpdate());
                key.setOnDelete(fkey.onDelete());
                key.setDeferable(fkey.deferable());
            }
        }
    }

    //

    public static String getSchemaName(Class<?> cls)
    {
        SQLSchema schema = cls.getAnnotation(SQLSchema.class);
        if (schema != null) { return schema.name(); }
        throw new RuntimeException("The class " + cls.getCanonicalName() + " must be annotated with SQLSchema()");
    }
    
    public static String getSchemaVersion(Class<?> cls)
    {
        SQLSchema schema = cls.getAnnotation(SQLSchema.class);
        if (schema != null) { return schema.version().major() + "." + schema.version().minor() + "." + schema.version().patch(); }
        throw new RuntimeException("The class " + cls.getCanonicalName() + " must be annotated with SQLSchema()");
    }

    public static String getColumnName(Field field)
    {
        SQLColumn sa = field.getAnnotation(SQLColumn.class);
        if (sa != null) { return sa.name(); }
        return null;
    }

    public static String getTableName(Class<?> cls)
    {
        SQLTable anno = cls.getAnnotation(SQLTable.class);
        if (anno != null) { return anno.name(); }
        throw new RuntimeException("The class: " + cls.getCanonicalName() + " must be annotated with SQLTable()");
    }

    public static boolean isSQLTable(Class<?> cls)
    {
        return cls.getAnnotation(SQLTable.class) != null;
    }

    public static Class<?> listOf(Method method)
    {
        ListOf listOf = method.getAnnotation(ListOf.class);
        if (listOf != null) return listOf.value();
        return null;
    }

    public static boolean returnsList(Method method)
    {
        return List.class == method.getReturnType();
    }

    public static Class<?> functionReturnType(Method method)
    {
        if (returnsList(method))
        {
            Class<?> type = listOf(method);
            if (type == null) throw new RuntimeException("The method " + method + " returns a list, you must annotate it with ListOf().");
            return type;
        }
        return method.getReturnType();
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getParameterAnnotation(Annotation[] annotations, Class<T> type)
    {
        for (Annotation anno : annotations)
        {
            if (type == anno.annotationType())
                return (T) anno;
        }
        return null;
    }
}
