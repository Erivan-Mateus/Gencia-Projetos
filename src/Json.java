import java.util.*;
/** JSON mínimo, sem dependências externas. */
public final class Json {
    public static String stringify(Object x){
        if(x==null)return "null"; if(x instanceof Boolean||x instanceof Number)return x.toString();
        if(x instanceof Map){StringJoiner j=new StringJoiner(",","{","}");((Map<?,?>)x).forEach((k,v)->j.add(stringify(k.toString())+":"+stringify(v)));return j.toString();}
        if(x instanceof Iterable){StringJoiner j=new StringJoiner(",","[","]");for(Object v:(Iterable<?>)x)j.add(stringify(v));return j.toString();}
        StringBuilder b=new StringBuilder("\"");for(char c:x.toString().toCharArray()){switch(c){case '\\':b.append("\\\\");break;case '"':b.append("\\\"");break;case '\n':b.append("\\n");break;case '\r':b.append("\\r");break;case '\t':b.append("\\t");break;default:if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}return b.append('"').toString();
    }
    public static Object parse(String s){Parser p=new Parser(s);Object v=p.val();p.ws();if(p.i!=s.length())throw new IllegalArgumentException("JSON inválido");return v;}
    private static class Parser {String s;int i;Parser(String s){this.s=s;}void ws(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}char peek(){ws();return i<s.length()?s.charAt(i):'\0';}void expect(char c){if(peek()!=c)throw new IllegalArgumentException("JSON inválido");i++;}Object val(){char c=peek();if(c=='"')return str();if(c=='{'){i++;Map<String,Object> m=new LinkedHashMap<>();if(peek()!='}')while(true){String k=str();expect(':');m.put(k,val());if(peek()!=',')break;i++;}expect('}');return m;}if(c=='['){i++;List<Object> a=new ArrayList<>();if(peek()!=']')while(true){a.add(val());if(peek()!=',')break;i++;}expect(']');return a;}for(String t:List.of("true","false","null"))if(s.startsWith(t,i)){i+=t.length();return t.equals("null")?null:Boolean.valueOf(t);}int st=i;if(c=='-')i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(st==i)throw new IllegalArgumentException("JSON inválido");return Long.parseLong(s.substring(st,i));}
    String str(){expect('"');StringBuilder b=new StringBuilder();while(i<s.length()){char c=s.charAt(i++);if(c=='"')return b.toString();if(c=='\\'){if(i==s.length())break;char e=s.charAt(i++);switch(e){case 'n':b.append('\n');break;case 'r':b.append('\r');break;case 't':b.append('\t');break;case 'b':b.append('\b');break;case 'f':b.append('\f');break;case 'u':b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;break;case '"':case '\\':case '/':b.append(e);break;default:throw new IllegalArgumentException("JSON inválido");}}else b.append(c);}throw new IllegalArgumentException("JSON inválido");}}
    public static Map<String,Object> obj(Object... a){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<a.length;i+=2)m.put((String)a[i],a[i+1]);return m;}
    @SuppressWarnings("unchecked") public static Map<String,Object> map(Object x){return (Map<String,Object>)x;}
    @SuppressWarnings("unchecked") public static List<Object> list(Object x){return (List<Object>)x;}
    public static String str(Map<String,Object> m,String k){Object v=m.get(k);if(!(v instanceof String))throw new IllegalArgumentException("Campo inválido: "+k);return (String)v;}
    public static int num(Map<String,Object> m,String k){Object v=m.get(k);if(!(v instanceof Number))throw new IllegalArgumentException("Campo inválido: "+k);return Math.toIntExact(((Number)v).longValue());}
    public static boolean bool(Map<String,Object> m,String k){Object v=m.get(k);if(!(v instanceof Boolean))throw new IllegalArgumentException("Campo inválido: "+k);return (Boolean)v;}
}
