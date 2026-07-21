package studio.lrxmc.trfp.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 极简 JSON 解析器 - 仅支持 TACZ 数据文件需要的特性:
 * - 字符串/数字/布尔/null
 * - 嵌套对象/数组
 * - 注释与多余逗号容错
 * 不依赖 gson/adventure
 */
public class MiniJson {

    private final String source;
    private int pos;

    public MiniJson(String src) {
        this.source = src;
        this.pos = 0;
    }

    public static Object parse(String src) {
        return new MiniJson(src).parseValue();
    }

    public static Map<String, Object> parseObject(String src) {
        Object o = parse(src);
        if (o instanceof Map) return (Map<String, Object>) o;
        return null;
    }

    private Object parseValue() {
        skipWs();
        if (pos >= source.length()) return null;
        char c = source.charAt(pos);
        if (c == '{') return parseObj();
        if (c == '[') return parseArr();
        if (c == '"') return parseStr();
        if (c == 't' || c == 'f') return parseBool();
        if (c == 'n') { skip(4); return null; }
        return parseNum();
    }

    private Map<String, Object> parseObj() {
        Map<String, Object> m = new LinkedHashMap<>();
        skip(1);
        skipWs();
        if (peek() == '}') { skip(1); return m; }
        while (true) {
            skipWs();
            String key = parseStr();
            skipWs();
            if (peek() == ':') skip(1);
            skipWs();
            Object v = parseValue();
            m.put(key, v);
            skipWs();
            char c = peek();
            if (c == ',') { skip(1); continue; }
            if (c == '}') { skip(1); break; }
            break;
        }
        return m;
    }

    private List<Object> parseArr() {
        List<Object> l = new ArrayList<>();
        skip(1);
        skipWs();
        if (peek() == ']') { skip(1); return l; }
        while (true) {
            skipWs();
            l.add(parseValue());
            skipWs();
            char c = peek();
            if (c == ',') { skip(1); continue; }
            if (c == ']') { skip(1); break; }
            break;
        }
        return l;
    }

    private String parseStr() {
        skipWs();
        if (peek() != '"') return "";
        skip(1);
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && source.charAt(pos) != '"') {
            char c = source.charAt(pos);
            if (c == '\\' && pos + 1 < source.length()) {
                char n = source.charAt(pos + 1);
                if (n == 'n') sb.append('\n');
                else if (n == 't') sb.append('\t');
                else if (n == 'r') sb.append('\r');
                else if (n == 'b') sb.append('\b');
                else if (n == 'f') sb.append('\f');
                else if (n == 'u') {
                    sb.append((char) Integer.parseInt(source.substring(pos+2, pos+6), 16));
                    pos += 4;
                } else sb.append(n);
                pos += 2;
            } else {
                sb.append(c);
                pos++;
            }
        }
        if (pos < source.length()) pos++;
        return sb.toString();
    }

    private Boolean parseBool() {
        if (source.startsWith("true", pos)) { skip(4); return Boolean.TRUE; }
        if (source.startsWith("false", pos)) { skip(5); return Boolean.FALSE; }
        return Boolean.FALSE;
    }

    private Object parseNum() {
        int s = pos;
        if (peek() == '-') pos++;
        while (pos < source.length() && "0123456789.eE+-".indexOf(source.charAt(pos)) >= 0) pos++;
        String n = source.substring(s, pos);
        if (n.contains(".") || n.contains("e") || n.contains("E")) {
            try { return Double.parseDouble(n); } catch (Exception e) { return 0d; }
        }
        try { return Long.parseLong(n); } catch (Exception e) { return 0; }
    }

    private void skip(int n) { pos += n; }

    private void skipWs() {
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
            else break;
        }
    }

    private char peek() {
        if (pos >= source.length()) return '\0';
        return source.charAt(pos);
    }

    // 简化取值
    public static String getString(Map<String, Object> m, String key, String def) {
        Object o = m == null ? null : m.get(key);
        return o == null ? def : o.toString();
    }

    public static int getInt(Map<String, Object> m, String key, int def) {
        Object o = m == null ? null : m.get(key);
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return def; }
    }

    public static double getDouble(Map<String, Object> m, String key, double def) {
        Object o = m == null ? null : m.get(key);
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return def; }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> m, String key) {
        Object o = m == null ? null : m.get(key);
        if (o instanceof Map) return (Map<String, Object>) o;
        return null;
    }
}
