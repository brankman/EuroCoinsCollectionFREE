package moedas.euro.free;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.sql.SQLException;

public class Database {

    private static final String DATABASE_NAME = "BDEuro";
    private static final int DATABASE_VERSION = 1;
    private final Context mContext;
    private DatabaseHelper myDBase;
    private SQLiteDatabase mDB;

    public Database(Context context) {
        this.mContext = context;
    }

    public Database open() throws SQLException {
        myDBase = new DatabaseHelper(mContext);
        mDB=myDBase.getWritableDatabase();
        return this;
    }

    /** Called when the activity is first created. */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        /* Create a Database. */
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
		    /* Create Tables in the Database. */
            db.execSQL("CREATE TABLE IF NOT EXISTS Countries (idCountry INTEGER PRIMARY KEY AUTOINCREMENT, country_num INTEGER, country_name VARCHAR, country_flag BLOB, country_flag_mini BLOB);");
            db.execSQL("CREATE TABLE IF NOT EXISTS Coins (idCoin INTEGER PRIMARY KEY AUTOINCREMENT, country_num INTEGER, coin_name VARCHAR, coin_img BLOB, coin_state INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/* Drop in the Database. */
            db.execSQL("DROP TABLE Countries;");
            db.execSQL("DROP TABLE Coins;");

            db.execSQL("CREATE TABLE Countries (idCountry INTEGER PRIMARY KEY AUTOINCREMENT, country_num INTEGER, country_name VARCHAR, country_flag BLOB, country_flag_mini BLOB);");
            db.execSQL("CREATE TABLE Coins (idCoin INTEGER PRIMARY KEY AUTOINCREMENT, country_num INTEGER, coin_name VARCHAR, coin_img BLOB, coin_state INTEGER);");

        }
    }

    /**############################## PROCURAR PARA A LISTA DE RESULTADOS #################################**/
    /*select para obter a informação necessária a:
    *   - lista de paises das moedas normais
    *   - lista de paises das moedas normais nas estatisticas
    * Nota: devolte todos os Países menos "PAE" (Paises Área Euro) que representam todos os paises para as moedas comemorativas */
    public Cursor selectCountryCoins() {
        Cursor c=mDB.rawQuery("SELECT idCountry as _id, country_name, country_flag " +
                "FROM Countries " +
                "WHERE idCountry <> 24 " +
                "ORDER BY country_name;", null);
        //Log.i("SELECT", "SELECT idCountry as _id, country_name, country_flag FROM Countries WHERE idCountry <> 24 ORDER BY country_name;");
        return c;
    }

    /*select para obter moedas tenho de cada pais para moedas normais
    *    - na lista de paises das moedas normais
    *    - na estatistica para o calcula da percentagem completa
    * Nota: */
    public Cursor selectCountryCoinsCount(String country_name) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalCoinsCountry' " +
                "FROM Coins cn, Countries ct " +
                "WHERE cn.coin_country = ct.country_num" +
                " AND cn.coin_state = 1 " +
                " AND cn.coin_isCommemorative = 0" +
                " AND ct.country_name='"+country_name+"';", null);
        //Log.i("SELECT", "SELECT COUNT(*) as 'totalCoinsCountry' FROM Coins cn, Countries ct WHERE cn.coin_country = ct.country_num AND cn.coin_state = 1 AND cn.coin_isCommemorative = 0 AND ct.country_name='"+country_name+"';");
        return c;
    }

    /*select para obter informação para montar a lista das moedas normais para o pais selecionado
    * Nota: */
    public Cursor selectCountryCoinsHave(int country_num) {
        Cursor c=mDB.rawQuery("SELECT cn.id as _id, cn.coin_code, ct.country_name, ct.country_flag, cn.coin_image, cn.coin_value, cn.coin_state " +
                "FROM Coins cn, Countries ct " +
                "WHERE cn.coin_country = ct.country_num" +
                " AND cn.coin_isCommemorative = 0" +
                " AND cn.coin_country="+country_num+";", null);
        //Log.i("SELECT", "SELECT cn.id as _id, cn.coin_code, ct.country_name, ct.country_flag_mini, cn.coin_image, cn.coin_value, cn.coin_state FROM Coins cn, Countries ct WHERE cn.coin_country = ct.country_num AND cn.coin_isCommemorative = 0 AND cn.coin_country="+country_num+";");
        return c;
    }

    /*select Para obter informação para montar a dialog das moedas normais qd selecionada uma moeda
    * Nota: */
    public Cursor selectCountryCoinDataDialog(long idCoin) {
        Cursor c=mDB.rawQuery("SELECT id as _id, coin_image, coin_value FROM Coins WHERE id="+idCoin+";", null);
        //Log.i("SELECT", );
        return c;
    }


    /*select para obter a informação necessária a:
    *   - lista de anos das moedas comemorativas
    *   - lista de anos das moedas comemorativas nas estatisticas
    * Nota: */
    public Cursor selectMoedasComemorativasAno() {
        Cursor c=mDB.rawQuery("SELECT DISTINCT coin_year as _id " +
                "FROM Coins " +
                "WHERE coin_isCommemorative = 1  " +
                "ORDER BY coin_year;", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obter a informação necessária a:
    *   - lista de paises das moedas comemorativas
    * Nota: */
    public Cursor selectMoedasComemorativasPais() {
        Cursor c=mDB.rawQuery("SELECT " +
                "cnt.country_num as _id, " +
                "cnt.country_name, " +
                "cnt.country_flag, " +
                "(  SELECT COUNT(*) " +
                "   FROM Coins cn, Countries ct " +
                "   WHERE cn.coin_country = ct.country_num " +
                "       AND cn.coin_isCommemorative = 1 " +
                "       AND ct.country_name=cnt.country_name  ) AS country_count " +
                "FROM Countries cnt " +
                "WHERE country_count > 0 " +
                "GROUP BY _id " +
                "ORDER BY cnt.country_name;", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obter informação para montar a lista das moedas comemorativas para o ano selecionado
    * Nota: */
    public Cursor selectMoedasComemorativasTenho(long ano) {
        Cursor c=mDB.rawQuery("SELECT cn.id as _id, cn.coin_code, ct.country_name, cn.coin_image, cn.coin_state " +
                "FROM Coins cn, Countries ct " +
                "WHERE cn.coin_country = ct.country_num AND cn.coin_isCommemorative = 1 AND coin_year="+ano+";", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obter informação para montar a lista das moedas comemorativas para o pais selecionado
    * Nota: */
    public Cursor selectMoedasComemorativasCountryTenho(long coin_country) {
        Cursor c=mDB.rawQuery("SELECT cn.id as _id, cn.coin_code, ct.country_name, cn.coin_image, cn.coin_state, cn.coin_year " +
                "FROM Coins cn, Countries ct " +
                "WHERE cn.coin_country = ct.country_num " +
                "AND cn.coin_isCommemorative = 1 " +
                "AND coin_country="+coin_country+" " +
                "ORDER BY cn.coin_year ASC;", null);
        return c;
    }

    /*select Para obter informação para montar a dialog das moedas comemorativas qd selecionada uma moeda
    * Nota: */
    public Cursor selectMoedasComemorativasDialog(long id) {
        Cursor c=mDB.rawQuery("SELECT cn.id as _id, ct.country_name, cn.coin_image, cn.coin_description, cn.coin_year FROM Coins cn, Countries ct WHERE cn.coin_country = ct.country_num AND cn.coin_isCommemorative = 1 AND id='"+id+"';", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select que devolve o total de moedas comemorativas de um determinado ano*/
    public Cursor selectMoedasComemorativasCount(String ano) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalCom' FROM Coins WHERE coin_isCommemorative = 1 AND coin_year='"+ano+"';", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obter moedas que tenho de cada pais para moedas commemorativas
    *    - na lista de paises das moedas commemorativas organizadapor pais
    * Nota: */
    public Cursor selectMoedasComemorativasCountryCount(String country_name) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalCoinsCommemorativeCountry' " +
                "FROM Coins cn, Countries ct " +
                "WHERE cn.coin_country = ct.country_num " +
                "AND cn.coin_isCommemorative = 1 AND ct.country_name='"+country_name+"';", null);
        return c;
    }

    /*select para obter moedas tenho de cada ano para moedas comemorativas
    *    - na lista de anos das moedas comemorativas
    *    - na estatistica para o calcula da percentagem completa
    * Nota: */
    public Cursor selectMoedasComemorativasCountTenho(String ano) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalComTenho' FROM Coins WHERE coin_isCommemorative = 1 AND coin_year='"+ano+"' AND coin_state=1;", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obter moedas tenho de cada ano para moedas comemorativas
    *    - na lista de anos das moedas comemorativas
    *    - na estatistica para o calcula da percentagem completa
    * Nota: */
    public Cursor selectMoedasComemorativasCountryCountTenho(String country_name) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalComCountryTenho' " +
                "FROM Coins cn, Countries ct " +
                "WHERE cn.coin_country = ct.country_num AND cn.coin_state = 1 " +
                "AND cn.coin_isCommemorative = 1 AND ct.country_name='"+country_name+"';", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para devolver o total de moedas normais por pais
    * Nota: */
    public Cursor selectCountCoinsPerCountry(int coin_country) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalCoin_Country' FROM Coins WHERE coin_isCommemorative = 0 AND coin_country = "+coin_country+";", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para devolver o total de moedas comem por ano
    * Nota: */
    public Cursor selectCountCoinsPerYear(long ano) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalCoin_Year' FROM Coins WHERE coin_isCommemorative = 1 AND coin_year="+ano+";", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para devolver o total de moedas comem por pais
    * Nota: */
    public Cursor selectCountCoinsPerCountry(long coin_country) {
        Cursor c=mDB.rawQuery("SELECT COUNT(*) as 'totalCoin_Country' FROM Coins WHERE coin_isCommemorative = 1 AND coin_country = "+coin_country+";", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obtera sigla do pais por numCountrie [podeieria estar num array?]
    * Nota: */
    public Cursor selectCountryByNum(long country_num) {
        Cursor c=mDB.rawQuery("SELECT country_num as _id, country_name FROM Countries WHERE country_num = "+country_num+";", null);
        return c;
    }

    /*select para obter a lista de moedas que se têm para criar o CSV (ficheiro de back-up)
    * Nota: */
    public Cursor selectCoinsToCSV() {
        Cursor c=mDB.rawQuery("SELECT coin_code as _id, coin_code FROM Coins WHERE coin_state = 1", null);
        //Log.i("SELECT", );
        return c;
    }

    /*select para obter o count de moedas com estado a 1 (tenho a moeda) para realizar a importação.
    * Necessário porque query que não devolve valores rebenta no cursor
    * Nota: */
    public Cursor selectCheckHasCoinsToExport() {
        Cursor c=mDB.rawQuery("SELECT count(*) as _id FROM Coins WHERE coin_state = 1", null);
        //Log.i("SELECT", );
        return c;
    }

    /**############################## EDITA TABELA #################################**/
    /*Nota:*/
    public void editCoins(String coin_code, int coin_state) {
        mDB.execSQL("UPDATE Coins SET coin_state="+coin_state+" WHERE coin_code='"+coin_code+"';");
    }

    /*Nota:*/
    public void editAllCoins(int coin_country, int coin_state) {
        mDB.execSQL("UPDATE Coins SET coin_state="+coin_state+" WHERE coin_country="+coin_country+";");
    }

    /*Nota:*/
    public void editAllCommemorativeCoinsPerYear(long year, int coin_state) {
        mDB.execSQL("UPDATE Coins SET coin_state="+coin_state+" WHERE coin_year="+year+";");
    }

    /*Nota:*/
    public void editAllCommemorativeCoinsPerCountry(long coin_country, int coin_state) {
        mDB.execSQL("UPDATE Coins SET coin_state="+coin_state+" WHERE coin_country="+coin_country+" AND coin_isCommemorative = 1;");
    }

    /* Close a Database. */
    public void close(){
        mDB.close();
        myDBase.close();
    }
}
