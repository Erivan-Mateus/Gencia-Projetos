import java.util.*;import java.security.*;import javax.crypto.SecretKeyFactory;import javax.crypto.spec.PBEKeySpec;
final class Credenciais {
    static final int ITER=120000; static final SecureRandom RANDOM=new SecureRandom();
    static String hash(String senha){byte[] salt=new byte[16];RANDOM.nextBytes(salt);return Base64.getEncoder().encodeToString(salt)+":"+derive(senha,salt);}
    private static String derive(String senha,byte[] salt){PBEKeySpec spec=new PBEKeySpec(senha.toCharArray(),salt,ITER,256);try{return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());}catch(Exception e){throw new IllegalStateException(e);}finally{spec.clearPassword();}}
    static boolean confere(String senha,String hash){if(hash==null||hash.isEmpty()||senha.length()>128)return false;String[] p=hash.split(":",2);return p.length==2&&MessageDigest.isEqual(p[1].getBytes(java.nio.charset.StandardCharsets.UTF_8),derive(senha,Base64.getDecoder().decode(p[0])).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    static String normal(String s){return s.trim().toLowerCase(Locale.ROOT);}
    static String codigo(){return "AL-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase(Locale.ROOT);}
}
