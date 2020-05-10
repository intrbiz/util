package com.intrbiz.data.db.compiler.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.intrbiz.Util;
import com.intrbiz.data.cache.CacheInvalidate;
import com.intrbiz.data.cache.Cacheable;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.introspector.function.CustomIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.GetterIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.RemoveIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.SQLFunctionIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.SetterIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLCustom;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLFunction;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLIndex;
import com.intrbiz.data.db.compiler.meta.SQLIndexes;
import com.intrbiz.data.db.compiler.meta.SQLPartition;
import com.intrbiz.data.db.compiler.meta.SQLPartitioning;
import com.intrbiz.data.db.compiler.meta.SQLPatch;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSchema;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLUniques;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.ForeignKey;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Index;
import com.intrbiz.data.db.compiler.model.Partition;
import com.intrbiz.data.db.compiler.model.Partitioning;
import com.intrbiz.data.db.compiler.model.Patch;
import com.intrbiz.data.db.compiler.model.PrimaryKey;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.model.Unique;
import com.intrbiz.data.db.compiler.model.Version;
import com.intrbiz.data.db.compiler.util.SQLScript;
import com.intrbiz.data.db.compiler.util.TextUtil;
import com.intrbiz.data.db.util.DBTypeAdapter;
import com.intrbiz.metadata.ListOf;

public class SQLIntrospector
{
    private Map<Class<? extends Annotation>, SQLFunctionIntrospector> functionIntrospectors = new IdentityHashMap<Class<? extends Annotation>, SQLFunctionIntrospector>();

    // caches
    
    private Map<Class<?>, Schema> schemaCache = new IdentityHashMap<Class<?>, Schema>();

    private Map<Class<?>, Table> tableCache = new IdentityHashMap<Class<?>, Table>();

    private Map<Class<?>, Type> typeCache = new IdentityHashMap<Class<?>, Type>();

    public SQLIntrospector()
    {
        super();
        // default SQL function introspectors
        this.registerFunctionIntrospector(SQLGetter.class, new GetterIntrospector());
        this.registerFunctionIntrospector(SQLSetter.class, new SetterIntrospector());
        this.registerFunctionIntrospector(SQLRemove.class, new RemoveIntrospector());
        this.registerFunctionIntrospector(SQLCustom.class, new CustomIntrospector());
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

    public Schema buildSchema(SQLDialect dialect, Class<?> cls)
    {
        Schema schema = this.schemaCache.get(cls);
        if (schema == null)
        {
            schema = new Schema(getSchemaName(cls), cls);
            this.schemaCache.put(cls, schema);
            //
            SQLSchema sqlSchema = cls.getAnnotation(SQLSchema.class);
            schema.setVersion(new Version(sqlSchema.version()));
            // patches
            schema.addPatches(this.buildPatches(dialect, cls));
            for (Class<?> patchCls : sqlSchema.patches())
            {
                schema.addPatches(this.buildPatches(dialect, patchCls));
            }
            // tables
            for (Class<?> tCls : sqlSchema.tables())
            {
                // build the table
                Table table = this.buildTable(dialect, tCls);
                schema.addTable(table);
                // build the type from the table
                Type type = this.buildType(dialect, tCls);
                schema.addType(type);
                // build the patches from the table
                schema.addPatches(this.buildPatches(dialect, tCls));
            }
            // functions
            this.buildFunctions(dialect, cls, schema);
            // sort the patches
            Collections.sort(schema.getPatches());
        }
        return schema;
    }
    
    public List<Patch> buildPatches(SQLDialect dialect, Class<?> cls)
    {
        List<Patch> patches = new LinkedList<Patch>();
        for (Method method : cls.getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers()))
            {
                // forcefully enable invocation
                method.setAccessible(true);
                // create the match
                SQLPatch sqlPatch = method.getAnnotation(SQLPatch.class);
                if (sqlPatch != null)
                {
                    Patch patch = new Patch();
                    patch.setType(sqlPatch.type());
                    patch.setName(sqlPatch.name());
                    patch.setIndex(sqlPatch.index());
                    patch.setVersion(new Version(sqlPatch.version()));
                    patch.setSkip(sqlPatch.skip());
                    //
                    try
                    {
                        SQLScript script = (SQLScript) method.invoke(null, new Object[] {});
                        patch.setScript(script);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        throw new RuntimeException("Failed to get SQL script: " + method, e);
                    }
                    //
                    patches.add(patch);
                }
            }
        }
        return patches;
    }

    protected void buildFunctions(SQLDialect dialect, Class<?> cls, Schema schema)
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
                    // cache handling
                    function.setCacheable(method.getAnnotation(Cacheable.class) != null);
                    CacheInvalidate inv = method.getAnnotation(CacheInvalidate.class);
                    if (inv != null)
                    {
                        for (String s : inv.value())
                        {
                            function.getCacheInvalidate().add(s);
                        }
                    }
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

    public Type buildType(SQLDialect dialect, Class<?> cls)
    {
        Type type = this.typeCache.get(cls);
        if (type == null)
        {
            Table table = this.buildTable(dialect, cls);
            type = new Type("t_" + table.getName());
            this.typeCache.put(cls, type);
            type.setColumns(table.getColumns());
            type.setSince(table.getSince());
            type.finish();
        }
        return type;
    }

    public Table buildTable(SQLDialect dialect, Class<?> cls)
    {
        Table table = this.tableCache.get(cls);
        if (table == null)
        {
            SQLTable sqlTable = cls.getAnnotation(SQLTable.class);
            // get the schema
            Schema schema = this.buildSchema(dialect, sqlTable.schema());
            // the table
            table = new Table(schema, getTableName(cls), cls, sqlTable.virtual());
            this.tableCache.put(cls, table);
            // since
            table.setSince(new Version(sqlTable.since()));
            // build the table columns
            this.buildTable(dialect, cls, 0, table);
            // primary key
            table.setPrimaryKey(this.buildPrimaryKey(dialect, cls, table));
            // foreign keys
            this.buildForeignKeys(dialect, cls, table);
            // unqiues
            this.buildUniques(dialect, cls, table);
            // indexes
            this.buildIndexes(dialect, cls, table);
            // sort the columns
            table.finish();
        }
        return table;
    }

    protected void buildTable(SQLDialect dialect, Class<?> cls, int classIndex, Table model)
    {
        if (cls == null) return;
        // recurse up the inheritance hierarchy
        buildTable(dialect, cls.getSuperclass(), classIndex + 1, model);
        // build the list of columns
        for (Field field : cls.getDeclaredFields())
        {
            Column attr = buildColumn(dialect, field, cls, classIndex);
            if (attr != null) model.addColumn(attr);
        }
        // build partitioning
        this.buildPartitioning(dialect, cls, classIndex, model);
    }
    
    protected void buildPartitioning(SQLDialect dialect, Class<?> cls, int classIndex, Table model)
    {
        SQLPartitioning partitioning = cls.getAnnotation(SQLPartitioning.class);
        if (partitioning != null && partitioning.value().length > 0)
        {
            Partitioning parting = new Partitioning();
            for (SQLPartition partition : partitioning.value())
            {   
                Partition part = new Partition();
                part.setMode(partition.mode());
                for (String column : partition.on())
                {
                    Column col = model.getColumns().stream().filter(c -> column.equals(c.getName())).findAny().orElse(null);
                    if (col == null)
                        throw new RuntimeException("Cannot partition on " + column + " since it does not exist, for class " + cls.getSimpleName());
                    part.getOn().add(col);
                }
                part.setIndexOn(partition.indexOn());
                part.setIndexOnUsing(partition.indexOnUsing());
                parting.getPartitions().add(part);
            }
            model.setPartitioning(parting);
        }
    }

    @SuppressWarnings("unchecked")
    protected Column buildColumn(SQLDialect dialect, Field field, Class<?> cls, int classIndex)
    {
        SQLColumn sa = field.getAnnotation(SQLColumn.class);
        if (sa != null)
        {
            String name = getColumnName(field);
            // if the @SQLColumn annotation defines a explicit SQL type use that in preference
            SQLType type =  Util.isEmpty(sa.type()) ? dialect.getType(field.getType()) : dialect.getType(sa.type());
            // we can't check the types the adapter returns due to erasure :(
            if ((! type.isCompatibleWith(field.getType())) && sa.adapter() == SQLColumn.NullAdapter.class)
            {
                throw new RuntimeException("The field type: " + field.getType() + " is not compatible with the SQL Type: " + type.getSQLType() + " (" + type.getDefaultJavaType().getCanonicalName() + ")");
            }
            return new Column(classIndex, sa.index(), name, type, field, sa.notNull(), sa.adapter() == SQLColumn.NullAdapter.class ? null : (Class<? extends DBTypeAdapter<?,?>>) sa.adapter(), new Version(sa.since()));
        }
        return null;
    }
    
    protected PrimaryKey buildPrimaryKey(SQLDialect dialect, Class<?> cls, Table table)
    {
        PrimaryKey pkey = new PrimaryKey(table.getName() + "_pk");
        this.buildPrimaryKey(dialect, cls, table, pkey);
        return pkey.getColumns().isEmpty() ? null : pkey;
    }

    protected void buildPrimaryKey(SQLDialect dialect, Class<?> cls, Table table, PrimaryKey pkey)
    {
        if (cls == null) return;
        this.buildPrimaryKey(dialect, cls.getSuperclass(), table, pkey);
        //
        for (Field field : cls.getDeclaredFields())
        {
            if (field.getAnnotation(SQLPrimaryKey.class) != null)
            {
                String colName = getColumnName(field);
                Column column = table.findColumn(colName);
                if (column != null)
                {
                    pkey.addColumn(column);
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
                key.setReferences(this.buildTable(dialect, fkey.references()));
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
                // since
                key.setSince(new Version(fkey.since()));
            }
        }
    }
    
    protected void buildUniques(SQLDialect dialect, Class<?> cls, Table table)
    {
        if (cls == null) return;
        this.buildUniques(dialect, cls.getSuperclass(), table);
        // look on the table
        List<SQLUnique> tblUnq = new LinkedList<SQLUnique>();
        if (cls.getAnnotation(SQLUnique.class) != null)
        {
            tblUnq.add(cls.getAnnotation(SQLUnique.class));
        }
        if (cls.getAnnotation(SQLUniques.class) != null)
        {
            for (SQLUnique unq : cls.getAnnotation(SQLUniques.class).value())
            {
                tblUnq.add(unq);
            }
        }
        for (SQLUnique unq : tblUnq)
        {
            if (Util.isEmpty(unq.name())) throw new RuntimeException("No name given for unique constraint on table " + table.getName() + ". Caused by " + cls);
            if (unq.columns().length == 0) throw new RuntimeException("No columns given for unique constraint on table " + table.getName() + ". Caused by " + cls);
            //
            Unique u = new Unique();
            u.setName(table.getName() + "_" + unq.name());
            for (String colName : unq.columns())
            {
                Column col = table.findColumn(colName);
                if (col == null) throw new RuntimeException("The column " + colName + " does not exist on table " + table.getName() + ".  Caused by " + cls);
                u.addColumn(col);
            }
            table.addUnique(u);
        }
        // the fields
        for (Field field : cls.getDeclaredFields())
        {
            SQLUnique unq = field.getAnnotation(SQLUnique.class);
            if (unq != null)
            {
                Unique u = new Unique();
                // name
                u.setName(Util.isEmpty(unq.name()) ? table.getName() + "_" + getColumnName(field) + "_unq" : table.getName() + "_" + unq.name());
                // columns
                if (unq.columns().length == 0)
                {
                    Column col = table.findColumn(getColumnName(field));
                    u.addColumn(col);
                }
                else
                {
                    for (String colName : unq.columns())
                    {
                        Column col = table.findColumn(colName);
                        if (col == null) throw new RuntimeException("The column " + colName + " does not exist on table " + table.getName() + ".  Caused by " + field);
                        u.addColumn(col);
                    }
                }
                table.addUnique(u);
            }
        }
    }
    
    protected void buildIndexes(SQLDialect dialect, Class<?> cls, Table table)
    {
        if (cls == null) return;
        this.buildIndexes(dialect, cls.getSuperclass(), table);
        // Indexes on the table
        List<SQLIndex> tblIdx = new LinkedList<SQLIndex>();
        if (cls.getAnnotation(SQLIndex.class) != null)
        {
            tblIdx.add(cls.getAnnotation(SQLIndex.class));
        }
        if (cls.getAnnotation(SQLIndexes.class) != null)
        {
            for (SQLIndex idx : cls.getAnnotation(SQLIndexes.class).value())
            {
                tblIdx.add(idx);
            }
        }
        for (SQLIndex idx : tblIdx)
        {
            if (Util.isEmpty(idx.name())) throw new RuntimeException("No name given for index on table " + table.getName() + ". Caused by " + cls);
            if (idx.columns().length == 0) throw new RuntimeException("No columns given for indxex on table " + table.getName() + ". Caused by " + cls);
            //
            Index i = new Index();
            i.setName(table.getName() + "_" + idx.name());
            if (! Util.isEmpty(idx.using())) i.setUsing(idx.using());
            if (! Util.isEmpty(idx.expression())) i.setExpression(idx.expression());
            i.setSince(new Version(idx.since()));
            for (String colName : idx.columns())
            {
                Column col = table.findColumn(colName);
                if (col == null) throw new RuntimeException("The column " + colName + " does not exist on table " + table.getName() + ".  Caused by index " + idx.name() + " on" + cls);
                i.addColumn(col);
            }
            table.addIndex(i);
        }
        // Indexes on fields
        for (Field field : cls.getDeclaredFields())
        {
            SQLIndex idx = field.getAnnotation(SQLIndex.class);
            if (idx != null)
            {
                Index i = new Index();
                // name
                i.setName( Util.isEmpty(idx.name()) ? table.getName() + "_" + getColumnName(field) + "_idx" : table.getName() + "_" + idx.name());
                // columns
                if (idx.columns().length == 0)
                {
                    Column col = table.findColumn(getColumnName(field));
                    i.addColumn(col);
                }
                else
                {
                    for (String colName : idx.columns())
                    {
                        Column col = table.findColumn(colName);
                        if (col == null) throw new RuntimeException("The column " + colName + " does not exist on table " + table.getName() + ".  Caused by index on " + field);
                        i.addColumn(col);
                    }
                }
                table.addIndex(i);
            }
        }
    }

    //
    
    public static void assertSQLTable(Class<?> cls)
    {
        SQLTable table = cls.getAnnotation(SQLTable.class);
        if (table == null) throw new RuntimeException("The class " + cls.getCanonicalName() + " must be annotated with SQLTable()");
    }

    public static String getSchemaName(Class<?> cls)
    {
        SQLSchema schema = cls.getAnnotation(SQLSchema.class);
        if (schema != null) return schema.name();
        throw new RuntimeException("The class " + cls.getCanonicalName() + " must be annotated with SQLSchema()");
    }
    
    public static Version getSchemaVersion(Class<?> cls)
    {
        SQLSchema schema = cls.getAnnotation(SQLSchema.class);
        if (schema != null) return new Version(schema.version());
        throw new RuntimeException("The class " + cls.getCanonicalName() + " must be annotated with SQLSchema()");
    }

    public static String getColumnName(Field field)
    {
        SQLColumn sa = field.getAnnotation(SQLColumn.class);
        if (sa != null) return sa.name();
        return null;
    }

    public static String getTableName(Class<?> cls)
    {
        SQLTable anno = cls.getAnnotation(SQLTable.class);
        if (anno != null) return anno.name();
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
