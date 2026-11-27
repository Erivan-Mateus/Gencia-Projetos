import java.io.IOException;
import java.sql.*;
/** Estado transacional do piloto. Revisão otimista impede sobrescrever alterações de outra instância. */
final class BancoPostgres {
    private final String url,usuario,senha;
    private long revisao=-1;
    private BancoPostgres(String url,String usuario,String senha){this.url=url;this.usuario=usuario;this.senha=senha;}
    static BancoPostgres configurado(){
        String url=System.getenv("DB_URL"),user=System.getenv("DB_USER"),pass=System.getenv("DB_PASSWORD");
        if(url==null&&user==null&&pass==null&&System.getenv("RENDER")==null)return null;
        if(url==null||!url.startsWith("jdbc:postgresql://")||user==null||user.isBlank()||pass==null||pass.isBlank())throw new IllegalStateException("Configure DB_URL, DB_USER e DB_PASSWORD; não será usado JSON local no Render.");
        return new BancoPostgres(url,user,pass);
    }
    private Connection conectar()throws SQLException{java.util.Properties p=new java.util.Properties();p.setProperty("user",usuario);p.setProperty("password",senha);p.setProperty("connectTimeout","10");p.setProperty("socketTimeout","20");return DriverManager.getConnection(url,p);}
    String carregar()throws IOException{
        try(Connection c=conectar()){
            // O SQL deve ter sido executado antes do primeiro deploy.
            try(PreparedStatement st=c.prepareStatement("INSERT INTO gestao_privado.estado (id, revisao, dados) VALUES (1,0,CAST(? AS jsonb)) ON CONFLICT (id) DO NOTHING")){st.setString(1,Repositorio.seed());st.executeUpdate();}
            try(PreparedStatement st=c.prepareStatement("SELECT revisao, dados::text FROM gestao_privado.estado WHERE id=1");ResultSet r=st.executeQuery()){if(!r.next())throw new IOException("Estado ausente.");revisao=r.getLong(1);return r.getString(2);}
        }catch(SQLException e){throw new IOException("PostgreSQL indisponível ou schema.sql não executado (SQLState "+e.getSQLState()+").");}
    }
    void salvar(String json)throws IOException{
        try(Connection c=conectar();PreparedStatement st=c.prepareStatement("UPDATE gestao_privado.estado SET dados=CAST(? AS jsonb), revisao=revisao+1, atualizado_em=now() WHERE id=1 AND revisao=?")){
            st.setString(1,json);st.setLong(2,revisao);if(st.executeUpdate()!=1)throw new IOException("Conflito de revisão; recarregue os dados.");revisao++;
        }catch(SQLException e){throw new IOException("Gravação PostgreSQL falhou (SQLState "+e.getSQLState()+").");}
    }
}
