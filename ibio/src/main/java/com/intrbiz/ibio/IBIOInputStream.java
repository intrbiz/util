package com.intrbiz.ibio;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.intrbiz.ibio.IBIO.FieldMeta;
import com.intrbiz.ibio.IBIO.FieldValue;
import com.intrbiz.ibio.IBIO.TypeMeta;

public class IBIOInputStream
{
    protected DataInputStream in;
    
    protected int streamVersion = 0;
    
    protected boolean varLen = false;
    
    protected Map<Integer, TypeMeta> objectMetaCache = new TreeMap<Integer, TypeMeta>();
    
    public IBIOInputStream(InputStream in)
    {
        this.in = new DataInputStream(in);
    }
    
    public void readHeader() throws IOException
    {
        // read the magic
        byte[] magic = new byte[IBIO.Symbols.MAGIC.length];
        this.in.read(magic);
        if (! Arrays.equals(magic, IBIO.Symbols.MAGIC)) throw new IOException("Invalid header, the magic was not as expected!");
        // read the version
        this.streamVersion = this.in.read();
        if (this.streamVersion != IBIO.Versions.V1) throw new IOException("Invalid header, wrong version!");
        // read the flags
        int flags = this.in.read();
        this.varLen = (flags &0x01) == 0x01;
    }
    
    public String readString() throws IOException
    {
        int tc = this._readType();
        if (tc == IBIO.TypeCodes.NULL) return null;
        if (tc != IBIO.TypeCodes.STRING) throw new IOException("Stream error: expected String, got " + tc);
        return this._readString();
    }
    
    public byte readInt8() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.INT8) throw new IOException("Stream error: expected INT8, got " + tc);
        return this._readInt8();
    }
    
    public short readInt16() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.INT16) throw new IOException("Stream error: expected INT16, got " + tc);
        return this._readInt16();
    }
    
    public int readInt32() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.INT32) throw new IOException("Stream error: expected INT32, got " + tc);
        return this._readInt32();
    }
    
    public long readInt64() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.INT64) throw new IOException("Stream error: expected INT64, got " + tc);
        return this._readInt64();
    }
    
    public float readFloat() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.REAL) throw new IOException("Stream error: expected REAL, got " + tc);
        return this._readFloat();
    }
    
    public double readDouble() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.DP_REAL) throw new IOException("Stream error: expected DP_REAL, got " + tc);
        return this._readDouble();
    }
    
    public boolean readBoolean() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.BOOL) throw new IOException("Stream error: expected BOOL, got " + tc);
        return this._readBoolean();
    }
    
    public UUID readUUID() throws IOException
    {
        int tc = this._readType();
        if (tc == IBIO.TypeCodes.NULL) return null;
        if (tc != IBIO.TypeCodes.UUID) throw new IOException("Stream error: expected UUID, got " + tc);
        return this._readUUID();
    }
    
    public byte[] readBuffer() throws IOException
    {
        int tc = this._readType();
        if (tc == IBIO.TypeCodes.NULL) return null;
        if (tc != IBIO.TypeCodes.BUFFER) throw new IOException("Stream error: expected BUFFER, got " + tc);
        return this._readBuffer();
    }
    
    public Object readObject() throws IOException
    {
        int itc = this._readType();
        return this.readObject(itc);
    }
    
    public Object readObject(int itc) throws IOException
    {
        if (itc == IBIO.TypeCodes.NULL)
        {
            return null;
        }
        else if (itc == IBIO.TypeCodes.STRING)
        {
            return this._readString();
        }
        else if (itc == IBIO.TypeCodes.INT8)
        {
            return this._readInt8();
        }
        else if (itc == IBIO.TypeCodes.INT16)
        {
            return this._readInt16();
        }
        else if (itc == IBIO.TypeCodes.INT32)
        {
            return this._readInt32();
        }
        else if (itc == IBIO.TypeCodes.INT64)
        {
            return this._readInt64();
        }
        else if (itc == IBIO.TypeCodes.REAL)
        {
            return this._readFloat();
        }
        else if (itc == IBIO.TypeCodes.DP_REAL)
        {
            return this._readDouble();
        }
        else if (itc == IBIO.TypeCodes.BOOL)
        {
            return this._readBoolean();
        }
        else if (itc == IBIO.TypeCodes.UUID)
        {
            return this._readUUID();
        }
        else if (itc == IBIO.TypeCodes.BUFFER)
        {
            return this._readBuffer();
        }
        else if (itc == IBIO.TypeCodes.LIST)
        {
            return this._readList();
        }
        else if (itc == IBIO.TypeCodes.MAP)
        {
            return this._readMap();
        }
        throw new IOException("Cannot deserailise value, type: " + itc);
    }
    
    public int readListStart() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.LIST) throw new IOException("Stream error: expected List, got " + tc);
        return this._readListStart();
    }
    
    public void readListSep() throws IOException
    {
        this._readListSep();
    }
    
    public void readListEnd() throws IOException
    {
        this._readListEnd();
    }
    
    public List<Object> readList() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.LIST) throw new IOException("Stream error: expected List, got " + tc);
        return this._readList();
    }
    
    public int readMapStart() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.MAP) throw new IOException("Stream error: expected Map, got " + tc);
        return this._readMapStart();
    }
    
    public void readMapFieldValueSep() throws IOException
    {
        this._readMapFieldValueSep();
    }
    
    public void readMapSep() throws IOException
    {
        this._readMapSep();
    }
    
    public void readMapEnd() throws IOException
    {
        this._readMapEnd();
    }
    
    public Map<String, ?> readMap() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.MAP) throw new IOException("Stream error: expected Map, got " + tc);
        return this._readMap();
    }
    
    public TypeMeta readObjectStart() throws IOException
    {
        int tc = this._readType();
        if (tc != IBIO.TypeCodes.OBJECT) throw new IOException("Stream error: expected Object, got " + tc);
        return this._readObjectStart();
    }
    
    public int readObjectField() throws IOException
    {
        return this._readObjectField();
    }
    
    public FieldValue readObjectField(TypeMeta meta) throws IOException
    {
        int fid = this._readObjectField();
        FieldMeta fm = meta.getField(fid);
        if (fm == null) throw new IOException("Stream error, unknown field in object");
        //
        int vtc = this._readType();
        if (vtc != IBIO.TypeCodes.NULL && vtc != fm.type) throw new IOException("Stream error, expecting type: " + fm.type + ", got: " + vtc + " for field: " + fid);
        Object val = this.readObject(vtc);
        return new FieldValue(fm, val);
    }
    
    public void readObjectSep() throws IOException
    {
        this._readObjectSep();
    }
    
    public void readObjectEnd() throws IOException
    {
        this._readObjectEnd();
    }
    
    public int readType() throws IOException
    {
        return this._readType();
    }
    
    // stream internals
    
    protected int _readType() throws IOException
    {
        return this.in.read();
    }
    
    protected byte _readInt8() throws IOException
    {
        return (byte) (this.in.read() & 0xFF);
    }
    
    protected short _readInt16() throws IOException
    {
        if (this.varLen)
        {
            short v = 0;
            for (int c = 0; c < 16; c += 7)
            {
                int b = this.in.read();
                v = (short) (v | ((b & 0x7F) << c));
                if ((b & 0x80) == 0) break;
            }
            return v;
        }
        else
        {
            return this.in.readShort();
        }
    }
    
    protected int _readInt32() throws IOException
    {
        if (this.varLen)
        {
            int v = 0;
            for (int c = 0; c < 32; c += 7)
            {
                int b = this.in.read();
                v = v | ((b & 0x7F) << c);
                if ((b & 0x80) == 0) break;
            }
            return v;
        }
        else
        {
            return this.in.readInt();
        }
    }
    
    protected long _readInt64() throws IOException
    {
        if (this.varLen)
        {
            long v = 0;
            for (int c = 0; c < 64; c += 7)
            {
                long b = this.in.read();
                v = v | ((b & 0x7FL) << c);
                if ((b & 0x80) == 0) break;
            }
            return v;
        }
        else
        {
            return this.in.readLong();
        }
    }
    
    protected float _readFloat() throws IOException
    {
        int i = this._readInt32();
        return Float.intBitsToFloat(i);
    }
    
    protected double _readDouble() throws IOException
    {
        long l = this._readInt64();
        return Double.longBitsToDouble(l);
    }
    
    protected boolean _readBoolean() throws IOException
    {
        return this.in.read() == 0x01;
    }
    
    protected UUID _readUUID() throws IOException
    {
        long msb = this.in.readLong();
        long lsb = this.in.readLong();
        return new UUID(msb, lsb);
    }
    
    protected String _readString() throws IOException
    {
        byte[] b = this._readBuffer();
        return new String(b, Charset.forName("UTF8"));
    }
    
    protected byte[] _readBuffer() throws IOException
    {
        int len = this._readInt32();
        byte[] b = new byte[len];
        this.in.readFully(b);
        return b;
    }
    
    protected int _readListStart() throws IOException
    {
        int len = this._readInt32();
        int b = this.in.read();
        if (b != IBIO.Symbols.LIST_START) throw new IOException("Stream error, expected list start, got " + b);
        return len;
    }
    
    protected void _readListSep() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.LIST_SEP) throw new IOException("Stream error, expected list sep, got " + b);
    }
    
    protected void _readListEnd() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.LIST_END) throw new IOException("Stream error, expected list end, got " + b);
    }
    
    protected List<Object> _readList() throws IOException
    {
        List<Object> l = new LinkedList<Object>();
        int len = this._readListStart();
        for (int i = 0; i < len; i++)
        {
            if (i > 0) this._readListSep();
            Object v = this.readObject();
            l.add(v);
        }
        this._readListEnd();
        return l;
    }
    
    protected int _readMapStart() throws IOException
    {
        int len = this._readInt32();
        int b = this.in.read();
        if (b != IBIO.Symbols.MAP_START) throw new IOException("Stream error, expected map start, got " + b);
        return len;
    }
    
    protected void _readMapFieldValueSep() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.MAP_FV_SEP) throw new IOException("Stream error, expected map field value sep, got " + b);
    }
    
    protected void _readMapSep() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.MAP_SEP) throw new IOException("Stream error, expected map sep, got " + b);
    }
    
    protected void _readMapEnd() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.MAP_END) throw new IOException("Stream error, expected map end, got " + b);
    }
    
    protected Map<String, ?> _readMap() throws IOException
    {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        int len = this._readMapStart();
        for (int i = 0 ; i < len; i++)
        {
            if (i > 0) this._readMapSep();
            String key = this._readString();
            this._readMapFieldValueSep();
            Object val = this.readObject();
            m.put(key, val);
        }
        this._readMapEnd();
        return m;
    }
    
    protected TypeMeta _readObjectStart() throws IOException
    {
        TypeMeta m = null;
        //
        boolean fullRecord = this._readBoolean();
        int     objectId   = this._readInt32();
        if (fullRecord)
        {
            String name    = this._readString();
            long   version = this._readInt64();
            int    fields  = this._readInt32();
            FieldMeta[] fmeta = new FieldMeta[fields];
            for (int i = 0; i < fields; i++)
            {
                int    fid   = this._readInt32();
                String fname = this._readString();
                int    ftype = this._readType();
                fmeta[i] = new FieldMeta(fname, fid, (byte) ftype);
            }
            m = new TypeMeta(name, version, fmeta);
            this.objectMetaCache.put(objectId, m);
        }
        else
        {
            m = this.objectMetaCache.get(objectId);
        }
        if (m == null) throw new IOException("Stream error, could not read type metadata");
        //
        int b = this.in.read();
        if (b != IBIO.Symbols.OBJECT_START) throw new IOException("Stream error, expected object start, got " + b);
        return m;
    }
    
    protected int _readObjectField() throws IOException
    {
        // read the id
        int id = this._readInt32();
        int b = this.in.read();
        if (b != IBIO.Symbols.OBJECT_FV_SEP) throw new IOException("Stream error, expected object field value sep, got " + b);
        return id;
    }
    
    protected void _readObjectSep() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.OBJECT_SEP) throw new IOException("Stream error, expected object sep, got " + b);
    }
    
    protected void _readObjectEnd() throws IOException
    {
        int b = this.in.read();
        if (b != IBIO.Symbols.OBJECT_END) throw new IOException("Stream error, expected object end, got " + b);
    }
}
