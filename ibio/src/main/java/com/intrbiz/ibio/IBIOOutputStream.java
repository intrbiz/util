package com.intrbiz.ibio;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import com.intrbiz.ibio.IBIO.FieldMeta;
import com.intrbiz.ibio.IBIO.TypeMeta;

public class IBIOOutputStream
{
    protected DataOutputStream out;

    protected boolean writenHeader = false;

    protected boolean varLen = false;
    
    protected boolean cacheTypeMeta = true;
    
    protected Map<String, TypeMetaCache> objectMetaCache = new TreeMap<String, TypeMetaCache>();
    
    protected int objectId = 0;

    public IBIOOutputStream(OutputStream out)
    {
        this.out = new DataOutputStream(out);
    }

    public boolean isVariableLengthEncoding()
    {
        return this.varLen;
    }

    public boolean isHeaderWritten()
    {
        return this.writenHeader;
    }

    /**
     * Write the stream header
     * 
     * @param version
     *            - the IO encoding version
     * @param varLen
     *            - use variable length encoding, optimises for size
     */
    public void writeHeader(int version, boolean varLen, boolean cacheTypeMeta) throws IOException
    {
        this.varLen = varLen;
        this.cacheTypeMeta = cacheTypeMeta;
        // write the magic
        this.out.write(IBIO.Symbols.MAGIC);
        // write the version and flags
        this.out.write(version);
        int flags = 0;
        if (varLen) flags = flags | 0x01;
        if (cacheTypeMeta) flags = flags | 0x02;
        this.out.write(flags);
        // mark the header as written
        this.writenHeader = true;
    }

    public void writeHeader(int version) throws IOException
    {
        this.writeHeader(version, true, true);
    }

    public void writeHeader() throws IOException
    {
        writeHeader(IBIO.Versions.V1);
    }

    //
    
    public void writeNull() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.NULL);
    }

    public void writeString(String s) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.STRING);
        this._writeString(s);
    }
    
    public void writeInt8(byte i) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.INT8);
        this._writeInt8(i);
    }
    
    public void writeInt16(short i) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.INT16);
        this._writeInt16(i);
    }

    public void writeInt32(int i) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.INT32);
        this._writeInt32(i);
    }
    
    public void writeInt64(long i) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.INT64);
        this._writeInt64(i);
    }
    
    public void writeFloat(float f) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.REAL);
        this._writeReal(f);
    }
    
    public void writeDouble(Double f) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.DP_REAL);
        this._writeDouble(f);
    }
    
    public void writeBool(boolean b) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.BOOL);
        this._writeBool(b);
    }
    
    public void writeUUID(UUID u) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.UUID);
        this._writeUUID(u);
    }
    
    public void writeBuffer(byte[] b) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        //
        this._writeType(IBIO.TypeCodes.BUFFER);
        this._writeBuffer(b);
    }

    //

    @SuppressWarnings("unchecked")
    public void writeObject(Object o) throws IOException
    {
        if (o == null)
        {
            this.writeNull();
        }
        else if (o instanceof String)
        {
            this.writeString((String) o);
        }
        else if (o instanceof Byte)
        {
            this.writeInt8((Byte) o);
        }
        else if (o instanceof Short)
        {
            this.writeInt16((Short) o);
        }
        else if (o instanceof Integer)
        {
            this.writeInt32((Integer) o);
        }
        else if (o instanceof Long)
        {
            this.writeInt64((Long) o);
        }
        else if (o instanceof Float)
        {
            this.writeFloat((Float) o);
        }
        else if (o instanceof Double)
        {
            this.writeDouble((Double) o);
        }
        else if (o instanceof Boolean)
        {
            this.writeBool((Boolean) o);
        }
        else if (o instanceof UUID)
        {
            this.writeUUID((UUID) o);
        }
        else if (o instanceof byte[])
        {
            this.writeBuffer((byte[]) o);
        }
        else if (o instanceof List)
        {
            this.writeList((List<?>) o);
        }
        else if (o instanceof Map)
        {
            this.writeMap((Map<String, ?>) o);
        }
        else
        {
            throw new IOException("Cannot serialise Object: " + o);
        }
    }

    //

    public void writeListStart(int len) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeType(IBIO.TypeCodes.LIST);
        this._writeListStart(len);
    }

    public void writeListSep() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeListSep();
    }

    public void writeListEnd() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeListEnd();
    }

    public void writeList(List<?> l) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this.writeListStart(l.size());
        boolean ns = false;
        for (Object o : l)
        {
            if (ns) this._writeListSep();
            this.writeObject(o);
            ns = true;
        }
        this.writeListEnd();
    }

    //

    public void writeMapStart(int len) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeType(IBIO.TypeCodes.MAP);
        this._writeMapStart(len);
    }

    public void writeMapEnd() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeMapEnd();
    }

    public void writeMapSep() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeMapSep();
    }

    public void writeMapField(String name) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeMapField(name);
    }

    public void writeMap(Map<String, ?> map) throws IOException
    {
        this.writeMapStart(map.size());
        boolean ns = false;
        for (Entry<String, ?> e : map.entrySet())
        {
            if (ns) this._writeMapSep();
            this._writeMapField(e.getKey());
            this.writeObject(e.getValue());
            ns = true;
        }
        this.writeMapEnd();
    }
    
    //
    
    public void writeObjectStart(TypeMeta meta) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        // cache the meta
        TypeMetaCache cache = this.objectMetaCache.get(meta.name);
        if (cache == null)
        {
            cache = new TypeMetaCache(meta, this.objectId++);
            this.objectMetaCache.put(meta.name, cache);
        }
        //
        this._writeType(IBIO.TypeCodes.OBJECT);
        this._writeObjectStart(cache);
    }

    public void writeObjectEnd() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeObjectEnd();
    }

    public void writeObjectSep() throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeObjectSep();
    }

    public void writeObjectField(int id) throws IOException
    {
        if (!this.writenHeader) throw new IOException("Header must be written first!");
        this._writeObjectField(id);
    }

    // stream internals
    protected void _writeType(byte code) throws IOException
    {
        this.out.write(code);
    }

    protected void _writeInt8(byte i) throws IOException
    {
        this.out.write(i);
    }

    protected void _writeInt16(short i) throws IOException
    {
        if (this.varLen)
        {
            do
            {
                int b = (int) (i & 0x7F);
                i = (short) (i >>> 7);
                if (i > 0) b = b | 0x80;
                this.out.write(b);
            }
            while (i > 0);
        }
        else
        {
            this.out.writeShort(i);
        }
    }

    protected void _writeInt32(int i) throws IOException
    {
        if (this.varLen)
        {
            do
            {
                int b = i & 0x7F;
                i = i >>> 7;
                if (i > 0) b = b | 0x80;
                this.out.write(b);
            }
            while (i > 0);
        }
        else
        {
            this.out.writeInt(i);
        }
    }

    protected void _writeInt64(long i) throws IOException
    {
        if (this.varLen)
        {
            do
            {
                int b = (int) (i & 0x7F);
                i = i >>> 7;
                if (i > 0) b = b | 0x80;
                this.out.write(b);
            }
            while (i > 0);
        }
        else
        {
            this.out.writeLong(i);
        }
    }

    protected void _writeReal(float real) throws IOException
    {
        this._writeInt32(Float.floatToIntBits(real));
    }

    protected void _writeDouble(double real) throws IOException
    {
        this._writeInt64(Double.doubleToLongBits(real));
    }

    protected void _writeBool(boolean b) throws IOException
    {
        this.out.write(b ? 0x01 : 0x00);
    }

    protected void _writeUUID(UUID id) throws IOException
    {
        this.out.writeLong(id.getMostSignificantBits());
        this.out.writeLong(id.getLeastSignificantBits());
    }

    protected void _writeString(String s) throws IOException
    {
        this._writeBuffer(s.getBytes(Charset.forName("UTF8")));
    }

    protected void _writeBuffer(byte[] b) throws IOException
    {
        // write the length of the buffer
        this._writeInt32(b.length);
        // write the data
        this.out.write(b, 0, b.length);
    }

    protected void _writeListStart(int len) throws IOException
    {
        this._writeInt32(len);
        this.out.write(IBIO.Symbols.LIST_START);
    }

    protected void _writeListSep() throws IOException
    {
        this.out.write(IBIO.Symbols.LIST_SEP);
    }

    protected void _writeListEnd() throws IOException
    {
        this.out.write(IBIO.Symbols.LIST_END);
    }

    protected void _writeMapStart(int len) throws IOException
    {
        this._writeInt32(len);
        this.out.write(IBIO.Symbols.MAP_START);
    }

    protected void _writeMapFieldValueSep() throws IOException
    {
        this.out.write(IBIO.Symbols.MAP_FV_SEP);
    }

    protected void _writeMapSep() throws IOException
    {
        this.out.write(IBIO.Symbols.MAP_SEP);
    }

    protected void _writeMapEnd() throws IOException
    {
        this.out.write(IBIO.Symbols.MAP_END);
    }

    public void _writeMapField(String name) throws IOException
    {
        this._writeString(name);
        this._writeMapFieldValueSep();
    }
    
    protected void _writeObjectStart(TypeMetaCache cache) throws IOException
    {
        // write the meta data
        if (cache.written)
        {
            this._writeBool(false);
            this._writeInt32(cache.id);
        }
        else
        {
            TypeMeta meta = cache.meta;
            this._writeBool(true);
            this._writeInt32(cache.id);
            //
            this._writeString(meta.name);
            this._writeInt64(meta.version);
            this._writeInt32(meta.fields.length);
            for (FieldMeta f : meta.fields)
            {
                this._writeInt32(f.id);
                this._writeString(f.name);
                this._writeType(f.type);
            }
            //
            cache.written = this.cacheTypeMeta;
        }
        //
        this.out.write(IBIO.Symbols.OBJECT_START);
    }

    protected void _writeObjectFieldValueSep() throws IOException
    {
        this.out.write(IBIO.Symbols.OBJECT_FV_SEP);
    }

    protected void _writeObjectSep() throws IOException
    {
        this.out.write(IBIO.Symbols.OBJECT_SEP);
    }

    protected void _writeObjectEnd() throws IOException
    {
        this.out.write(IBIO.Symbols.OBJECT_END);
    }

    public void _writeObjectField(int id) throws IOException
    {
        this._writeInt32(id);
        this._writeObjectFieldValueSep();
    }
    
    //

    public void flush() throws IOException
    {
        this.out.flush();
    }

    public void close() throws IOException
    {
        this.out.close();
    }
    
    //  
    protected static class TypeMetaCache
    {
        public final TypeMeta meta;
        public final int id;
        public boolean written = false;
        
        public TypeMetaCache(TypeMeta meta, int id)
        {
            this.meta = meta;
            this.id = id;
        }
    }
}
