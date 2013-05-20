package com.intrbiz.ibio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public final class IBIO
{
    public static final class TypeCodes
    {
        public static final byte INT8     = 'o';
        public static final byte INT16    = 's';
        public static final byte INT32    = 'i';
        public static final byte INT64    = 'l';
        public static final byte UINT8    = 'O';
        public static final byte UINT16   = 'S';
        public static final byte UINT32   = 'I';
        public static final byte UINT64   = 'L';
        public static final byte REAL     = 'f';
        public static final byte DP_REAL  = 'd';
        public static final byte BOOL     = 'b';
        public static final byte UUID     = 'u';
        public static final byte STRING   = 'C';
        public static final byte BUFFER   = 'B';
        public static final byte LIST     = 'A';
        public static final byte MAP      = 'M';
        public static final byte OBJECT   = 'T';
        public static final byte NULL     = 'n';
    }
    
    public static final class Symbols
    {
        public static final byte[] MAGIC       = { 'I', 'B', 'I', 'O' };
        //
        public static final byte LIST_START    = '[';
        public static final byte LIST_SEP      = ',';
        public static final byte LIST_END      = ']';
        //
        public static final byte MAP_START     = '{';
        public static final byte MAP_FV_SEP    = ':';
        public static final byte MAP_SEP       = ',';
        public static final byte MAP_END       = '}';
        //
        public static final byte OBJECT_START  = '(';
        public static final byte OBJECT_FV_SEP = ':';
        public static final byte OBJECT_SEP    = ',';
        public static final byte OBJECT_END    = ')';
    }
    
    public static final class Versions
    {
        public static final int V1 = 1;
    }
    
    public static final class TypeMeta
    {
        public final String name;
        public final long version;
        public final FieldMeta[] fields;
        
        public TypeMeta(String name, long version, FieldMeta... fields)
        {
            this.name = name;
            this.version = version;
            this.fields = fields;
        }
        
        public FieldMeta getField(int id)
        {
            for (FieldMeta fm : this.fields)
            {
                if (fm.id == id) return fm;
            }
            return null;
        }
    }
    
    public static final class FieldMeta
    {
        public final String name;
        public final int id;
        public final byte type;
        
        public FieldMeta(String name, int id, byte type)
        {
            this.name = name;
            this.id = id;
            this.type = type;
        }
    }
    
    public static final class FieldValue
    {
        public final FieldMeta field;
        public final Object value;
        
        public FieldValue(FieldMeta field, Object value)
        {
            this.field = field;
            this.value = value;
        }
    }
    
    //
    
    public static final void main(String[] args) throws Exception
    {
        List<Object> l = new LinkedList<Object>();
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        for (int i = 0 ; i < 5; i ++)
        {
            l.add("Hello " + i);
            m.put("Key " + i, "Value " + i);
        }
        //
        List<Object> gl = new LinkedList<Object>();
        gl.add("Start");
        gl.add(1);
        gl.add(2);
        gl.add(3);
        gl.add("End");
        gl.add(m);
        gl.add(l);
        gl.add(null);
        //
        Map<String, Object> gm = new LinkedHashMap<String, Object>();
        gm.put("A String", "String");
        gm.put("A Int", 12345678);
        gm.put("A List", l);
        gm.put("A Map", m);
        gm.put("A Bool (F)", false);
        gm.put("A Bool (T)", true);
        gm.put("A Long", 1234567891234L);
        gm.put("A Short", 8192);
        gm.put("A Byte", 255);
        gm.put("A UUID", UUID.randomUUID());
        gm.put("A Buffer", IBIO.Symbols.MAGIC);
        gm.put("A Float", 123.456F);
        gm.put("A Double", 123.456789D);
        gm.put("A Null", null);
        //
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IBIOOutputStream cos = new IBIOOutputStream(baos);
        cos.writeHeader(1, true, true);
        cos.writeString("Hello World");
        cos.writeList(l);
        cos.writeMap(m);
        cos.writeList(gl);
        cos.writeMap(gm);
        //
        TypeMeta tm = new TypeMeta("TestObject123", 1, 
                new FieldMeta("a_a_a_a_a_a",1, IBIO.TypeCodes.STRING),
                new FieldMeta("b_b_b_b_b_b",2, IBIO.TypeCodes.STRING),
                new FieldMeta("c_c_c_c_c_c",3, IBIO.TypeCodes.STRING),
                new FieldMeta("d_d_d_d_d_d",4, IBIO.TypeCodes.STRING),
                new FieldMeta("e_e_e_e_e_e",5, IBIO.TypeCodes.STRING),
                new FieldMeta("f_f_f_f_f_f",6, IBIO.TypeCodes.STRING),
                new FieldMeta("g_g_g_g_g_g",7, IBIO.TypeCodes.STRING),
                new FieldMeta("h_h_h_h_h_h",8, IBIO.TypeCodes.STRING),
                new FieldMeta("i_i_i_i_i_i",9, IBIO.TypeCodes.STRING),
                new FieldMeta("j_j_j_j_j_j",10, IBIO.TypeCodes.STRING)
        );
        cos.writeListStart(10);
        for (int i = 0; i < 10; i++)
        {
            if (i > 0) cos.writeListSep();
            cos.writeObjectStart(tm);
            cos.writeObjectField(1);
            cos.writeNull();
            cos.writeObjectSep();
            cos.writeObjectField(2);
            cos.writeString("Value for b");
            cos.writeObjectSep();
            cos.writeObjectField(3);
            cos.writeString("Value for c");
            cos.writeObjectSep();
            cos.writeObjectField(4);
            cos.writeString("Value for d");
            cos.writeObjectSep();
            cos.writeObjectField(5);
            cos.writeString("Value for e");
            cos.writeObjectSep();
            cos.writeObjectField(6);
            cos.writeString("Value for f");
            cos.writeObjectSep();
            cos.writeObjectField(7);
            cos.writeString("Value for g");
            cos.writeObjectSep();
            cos.writeObjectField(8);
            cos.writeString("Value for h");
            cos.writeObjectSep();
            cos.writeObjectField(9);
            cos.writeString("Value for i");
            cos.writeObjectSep();
            cos.writeObjectField(10);
            cos.writeString("Value for j");
            cos.writeObjectEnd();
        }
        cos.writeListEnd();
        //
        cos.close();
        //
        byte[] b = baos.toByteArray();
        for (int i = 0; i < b.length; i++)
        {
            if (i > 0)
            {
                System.out.print(" ");
                if (i % 4 == 0) System.out.print(" ");
                if (i % 16 == 0) System.out.println();
            }
            System.out.print((b[i] <= 15 && b[i] >= 0 ? "0" : "") + Integer.toHexString(b[i] & 0xFF).toUpperCase());
        }
        System.out.println("\r\nLen: " + b.length);
        System.out.println();
        //
        FileOutputStream fos = new FileOutputStream("test.bin");
        fos.write(b);
        fos.close();
        //
        IBIOInputStream cis = new IBIOInputStream(new ByteArrayInputStream(b));
        cis.readHeader();
        System.out.println(cis.readString());
        for (Object s : cis.readList())
        {
            System.out.println(s);    
        }
        for (Entry<String,?> e : cis.readMap().entrySet())
        {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        for (Object o : cis.readList())
        {
            System.out.println(o);    
        }
        for (Entry<String,?> e : cis.readMap().entrySet())
        {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        int len = cis.readListStart();
        for (int j = 0; j < len; j++)
        {
            if (j > 0) cis.readListSep();
            //
            TypeMeta otm = cis.readObjectStart();
            System.out.println("Object: " + otm.name);
            for (int i = 0; i < otm.fields.length; i++)
            {
                if (i > 0) cis.readObjectSep();
                FieldValue fm = cis.readObjectField(otm);
                System.out.println("Field " + fm.field.name + ": " + fm.value);
            }
            cis.readObjectEnd();
        }
        cis.readListEnd();
    }
}
