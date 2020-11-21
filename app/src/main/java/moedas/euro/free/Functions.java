package moedas.euro.free;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Hashtable;


public class Functions {
    Context mContext;
    static Database mDataBase;
    Hashtable<String, String> mapOldNewCodes_Normal = new Hashtable<String, String>();
    Hashtable<String, String> mapOldNewCodes_Commemorative = new Hashtable<String, String>();

    public Functions(Context context){
        this.mContext = context;
        mDataBase = new Database(mContext);

        /* este método cria uma hash com o mapeamento dos codigos antigos com os novos para a importação*/
        MapOldToNewCodes_Normal();
        MapOldToNewCodes_Commemorative();
    }

    public void startAds(){
        //MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));
        //AdView mAdView = findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);
    }

    public void importInitialDB(){
        OutputStream myOutput;

        try {
            myOutput = new FileOutputStream("/data/data/moedas.euro.free/databases/BDEuro");

            /** Set the input file stream up: **/
            InputStream myInputs = mContext.getAssets().open("BDEuro");

            /** Transfer bytes from the input file to the output file **/
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInputs.read(buffer)) > 0)
            {
                myOutput.write(buffer, 0, length);
            }

            /** Close and clear the streams **/
            myOutput.flush();
            myOutput.close();
            myInputs.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeToCSV()
    {
        try {
            //File myFile = new File(Environment.getExternalStorageDirectory(), "/EuroCoinsCollection/coinsBackUp.csv");
            File myFile = new File(mContext.getExternalFilesDir(null), "/coinsBackUp.csv");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write("Cod.;State");
            myOutWriter.append("\n");

            try {
                mDataBase.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Cursor crs = mDataBase.selectCheckHasCoinsToExport();

            if (crs.moveToFirst()) {
                if( crs.getInt(crs.getColumnIndex("_id")) > 0) {
                    Cursor c = mDataBase.selectCoinsToCSV();

                    if (c != null) {
                        if (c.moveToFirst()) {
                            do {
                                String cod = c.getString(c.getColumnIndex("_id"));

                                myOutWriter.append(cod + ";Yes");
                                myOutWriter.append("\n");
                            }

                            while (c.moveToNext());
                        }
                    }
                    c.close();
                }
            }
            crs.close();

            myOutWriter.close();
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }

    public void readFromCSV(String filename)
    {
        try{
            //File myFile = new File(filename);
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String data = null;
            String read_code = null;

            try {
                mDataBase.open();

                while ((data = br.readLine()) != null) {
                    if(!data.equals("Cod.;State")) {
                        String[] sarray = data.split(";");
                        read_code = sarray[0];

                        mDataBase.editCoins(read_code, 1);
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            finally {
                br.close();
                fr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }



    /* INI - Deal with old files */
    public void readCSVNormal(String filename){
        try{
            //File myFile = new File(filename);
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String data = null;
            String read_id = null;

            try {
                mDataBase.open();

                while ((data = br.readLine()) != null) {
                    if(!data.equals("Cod.;País;Valor;Estado") && !data.equals("Cod.;Country;Value;State")) {
                        String[] sarray = data.split(";");
                        read_id = sarray[0];

                        String codNewDB = mapOldNewCodes_Normal.get(read_id);

                        if ( sarray[3].equals("Sim") || sarray[3].equals("Yes") ) {
                            mDataBase.editCoins(codNewDB, 1);
                        }
                        else if ( sarray[3].equals("Não") || sarray[3].equals("No") ) {
                            mDataBase.editCoins(codNewDB, 0);
                        }
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            finally {
                br.close();
                fr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }

    public void readCSVCommemorative(String filename){
        try{
            File myFile = new File(filename);
            FileReader fr = new FileReader(myFile);
            BufferedReader br = new BufferedReader(fr);
            String data = null;
            String read_id = null;

            try {
                mDataBase.open();

                while ((data = br.readLine()) != null) {
                    if(!data.equals("Cod.;País;Ano;Estado;Descrição") && !data.equals("Cod.;Country;Year;State;Description")) {
                        String[] sarray = data.split(";");
                        read_id = sarray[0];

                        String codNewDB = mapOldNewCodes_Commemorative.get(read_id);

                        if ( sarray[3].equals("Sim") || sarray[3].equals("Yes") ) {
                            mDataBase.editCoins(codNewDB, 1);
                        }
                        else if ( sarray[3].equals("Não") || sarray[3].equals("No") ) {
                            mDataBase.editCoins(codNewDB, 0);
                        }
                    }
                }
                br.close();
                fr.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }


    public void MapOldToNewCodes_Normal() {
        mapOldNewCodes_Normal.put("1","cn0001");
        mapOldNewCodes_Normal.put("2","cn0002");
        mapOldNewCodes_Normal.put("3","cn0003");
        mapOldNewCodes_Normal.put("4","cn0004");
        mapOldNewCodes_Normal.put("5","cn0005");
        mapOldNewCodes_Normal.put("6","cn0006");
        mapOldNewCodes_Normal.put("7","cn0007");
        mapOldNewCodes_Normal.put("8","cn0008");
        mapOldNewCodes_Normal.put("9","cn0009");
        mapOldNewCodes_Normal.put("10","cn0010");
        mapOldNewCodes_Normal.put("11","cn0011");
        mapOldNewCodes_Normal.put("12","cn0012");
        mapOldNewCodes_Normal.put("13","cn0013");
        mapOldNewCodes_Normal.put("14","cn0014");
        mapOldNewCodes_Normal.put("15","cn0015");
        mapOldNewCodes_Normal.put("16","cn0016");
        mapOldNewCodes_Normal.put("17","cn0017");
        mapOldNewCodes_Normal.put("18","cn0018");
        mapOldNewCodes_Normal.put("19","cn0019");
        mapOldNewCodes_Normal.put("20","cn0020");
        mapOldNewCodes_Normal.put("21","cn0021");
        mapOldNewCodes_Normal.put("22","cn0022");
        mapOldNewCodes_Normal.put("23","cn0023");
        mapOldNewCodes_Normal.put("24","cn0024");
        mapOldNewCodes_Normal.put("25","cn0025");
        mapOldNewCodes_Normal.put("26","cn0026");
        mapOldNewCodes_Normal.put("27","cn0027");
        mapOldNewCodes_Normal.put("28","cn0028");
        mapOldNewCodes_Normal.put("29","cn0029");
        mapOldNewCodes_Normal.put("30","cn0030");
        mapOldNewCodes_Normal.put("31","cn0031");
        mapOldNewCodes_Normal.put("32","cn0032");
        mapOldNewCodes_Normal.put("33","cn0033");
        mapOldNewCodes_Normal.put("34","cn0034");
        mapOldNewCodes_Normal.put("35","cn0035");
        mapOldNewCodes_Normal.put("36","cn0036");
        mapOldNewCodes_Normal.put("37","cn0037");
        mapOldNewCodes_Normal.put("38","cn0038");
        mapOldNewCodes_Normal.put("39","cn0039");
        mapOldNewCodes_Normal.put("40","cn0040");
        mapOldNewCodes_Normal.put("41","cn0041");
        mapOldNewCodes_Normal.put("42","cn0042");
        mapOldNewCodes_Normal.put("43","cn0043");
        mapOldNewCodes_Normal.put("44","cn0044");
        mapOldNewCodes_Normal.put("45","cn0045");
        mapOldNewCodes_Normal.put("46","cn0046");
        mapOldNewCodes_Normal.put("47","cn0047");
        mapOldNewCodes_Normal.put("48","cn0048");
        mapOldNewCodes_Normal.put("49","cn0049");
        mapOldNewCodes_Normal.put("50","cn0050");
        mapOldNewCodes_Normal.put("51","cn0051");
        mapOldNewCodes_Normal.put("52","cn0052");
        mapOldNewCodes_Normal.put("53","cn0053");
        mapOldNewCodes_Normal.put("54","cn0054");
        mapOldNewCodes_Normal.put("55","cn0055");
        mapOldNewCodes_Normal.put("56","cn0056");
        mapOldNewCodes_Normal.put("57","cn0057");
        mapOldNewCodes_Normal.put("58","cn0058");
        mapOldNewCodes_Normal.put("59","cn0059");
        mapOldNewCodes_Normal.put("60","cn0060");
        mapOldNewCodes_Normal.put("61","cn0061");
        mapOldNewCodes_Normal.put("62","cn0062");
        mapOldNewCodes_Normal.put("63","cn0063");
        mapOldNewCodes_Normal.put("64","cn0064");
        mapOldNewCodes_Normal.put("65","cn0065");
        mapOldNewCodes_Normal.put("66","cn0066");
        mapOldNewCodes_Normal.put("67","cn0067");
        mapOldNewCodes_Normal.put("68","cn0068");
        mapOldNewCodes_Normal.put("69","cn0069");
        mapOldNewCodes_Normal.put("70","cn0070");
        mapOldNewCodes_Normal.put("71","cn0071");
        mapOldNewCodes_Normal.put("72","cn0072");
        mapOldNewCodes_Normal.put("73","cn0073");
        mapOldNewCodes_Normal.put("74","cn0074");
        mapOldNewCodes_Normal.put("75","cn0075");
        mapOldNewCodes_Normal.put("76","cn0076");
        mapOldNewCodes_Normal.put("77","cn0077");
        mapOldNewCodes_Normal.put("78","cn0078");
        mapOldNewCodes_Normal.put("79","cn0079");
        mapOldNewCodes_Normal.put("80","cn0080");
        mapOldNewCodes_Normal.put("81","cn0081");
        mapOldNewCodes_Normal.put("82","cn0082");
        mapOldNewCodes_Normal.put("83","cn0083");
        mapOldNewCodes_Normal.put("84","cn0084");
        mapOldNewCodes_Normal.put("85","cn0085");
        mapOldNewCodes_Normal.put("86","cn0086");
        mapOldNewCodes_Normal.put("87","cn0087");
        mapOldNewCodes_Normal.put("88","cn0088");
        mapOldNewCodes_Normal.put("89","cn0089");
        mapOldNewCodes_Normal.put("90","cn0090");
        mapOldNewCodes_Normal.put("91","cn0091");
        mapOldNewCodes_Normal.put("92","cn0092");
        mapOldNewCodes_Normal.put("93","cn0093");
        mapOldNewCodes_Normal.put("94","cn0094");
        mapOldNewCodes_Normal.put("95","cn0095");
        mapOldNewCodes_Normal.put("96","cn0096");
        mapOldNewCodes_Normal.put("97","cn0097");
        mapOldNewCodes_Normal.put("98","cn0098");
        mapOldNewCodes_Normal.put("99","cn0099");
        mapOldNewCodes_Normal.put("100","cn0100");
        mapOldNewCodes_Normal.put("101","cn0101");
        mapOldNewCodes_Normal.put("102","cn0102");
        mapOldNewCodes_Normal.put("103","cn0103");
        mapOldNewCodes_Normal.put("104","cn0104");
        mapOldNewCodes_Normal.put("105","cn0105");
        mapOldNewCodes_Normal.put("106","cn0106");
        mapOldNewCodes_Normal.put("107","cn0107");
        mapOldNewCodes_Normal.put("108","cn0108");
        mapOldNewCodes_Normal.put("109","cn0109");
        mapOldNewCodes_Normal.put("110","cn0110");
        mapOldNewCodes_Normal.put("111","cn0111");
        mapOldNewCodes_Normal.put("112","cn0112");
        mapOldNewCodes_Normal.put("113","cn0113");
        mapOldNewCodes_Normal.put("114","cn0114");
        mapOldNewCodes_Normal.put("115","cn0115");
        mapOldNewCodes_Normal.put("116","cn0116");
        mapOldNewCodes_Normal.put("117","cn0117");
        mapOldNewCodes_Normal.put("118","cn0118");
        mapOldNewCodes_Normal.put("119","cn0119");
        mapOldNewCodes_Normal.put("120","cn0120");
        mapOldNewCodes_Normal.put("121","cn0121");
        mapOldNewCodes_Normal.put("122","cn0122");
        mapOldNewCodes_Normal.put("123","cn0123");
        mapOldNewCodes_Normal.put("124","cn0124");
        mapOldNewCodes_Normal.put("125","cn0125");
        mapOldNewCodes_Normal.put("126","cn0126");
        mapOldNewCodes_Normal.put("127","cn0127");
        mapOldNewCodes_Normal.put("128","cn0128");
        mapOldNewCodes_Normal.put("129","cn0129");
        mapOldNewCodes_Normal.put("130","cn0130");
        mapOldNewCodes_Normal.put("131","cn0131");
        mapOldNewCodes_Normal.put("132","cn0132");
        mapOldNewCodes_Normal.put("133","cn0133");
        mapOldNewCodes_Normal.put("134","cn0134");
        mapOldNewCodes_Normal.put("135","cn0135");
        mapOldNewCodes_Normal.put("136","cn0136");
        mapOldNewCodes_Normal.put("137","cn0137");
        mapOldNewCodes_Normal.put("138","cn0138");
        mapOldNewCodes_Normal.put("139","cn0139");
        mapOldNewCodes_Normal.put("140","cn0140");
        mapOldNewCodes_Normal.put("141","cn0141");
        mapOldNewCodes_Normal.put("142","cn0142");
        mapOldNewCodes_Normal.put("143","cn0143");
        mapOldNewCodes_Normal.put("144","cn0144");
        mapOldNewCodes_Normal.put("145","cn0145");
        mapOldNewCodes_Normal.put("146","cn0146");
        mapOldNewCodes_Normal.put("147","cn0147");
        mapOldNewCodes_Normal.put("148","cn0148");
        mapOldNewCodes_Normal.put("149","cn0149");
        mapOldNewCodes_Normal.put("150","cn0150");
        mapOldNewCodes_Normal.put("151","cn0151");
        mapOldNewCodes_Normal.put("152","cn0152");
        mapOldNewCodes_Normal.put("153","cn0153");
        mapOldNewCodes_Normal.put("154","cn0154");
        mapOldNewCodes_Normal.put("155","cn0155");
        mapOldNewCodes_Normal.put("156","cn0156");
        mapOldNewCodes_Normal.put("157","cn0157");
        mapOldNewCodes_Normal.put("158","cn0158");
        mapOldNewCodes_Normal.put("159","cn0159");
        mapOldNewCodes_Normal.put("160","cn0160");
    }

    public void MapOldToNewCodes_Commemorative() {
        mapOldNewCodes_Commemorative.put("1", "cn0185");
        mapOldNewCodes_Commemorative.put("2", "cn0186");
        mapOldNewCodes_Commemorative.put("3", "cn0187");
        mapOldNewCodes_Commemorative.put("4", "cn0188");
        mapOldNewCodes_Commemorative.put("5", "cn0189");
        mapOldNewCodes_Commemorative.put("6", "cn0190");
        mapOldNewCodes_Commemorative.put("7", "cn0191");
        mapOldNewCodes_Commemorative.put("8", "cn0192");
        mapOldNewCodes_Commemorative.put("9", "cn0193");
        mapOldNewCodes_Commemorative.put("10", "cn0194");
        mapOldNewCodes_Commemorative.put("11", "cn0195");
        mapOldNewCodes_Commemorative.put("12", "cn0196");
        mapOldNewCodes_Commemorative.put("13", "cn0197");
        mapOldNewCodes_Commemorative.put("14", "cn0198");
        mapOldNewCodes_Commemorative.put("15", "cn0199");
        mapOldNewCodes_Commemorative.put("16", "cn0200");
        mapOldNewCodes_Commemorative.put("17", "cn0201");
        mapOldNewCodes_Commemorative.put("18", "cn0202");
        mapOldNewCodes_Commemorative.put("19", "cn0203");
        mapOldNewCodes_Commemorative.put("20", "cn0204");
        mapOldNewCodes_Commemorative.put("21", "cn0205");
        mapOldNewCodes_Commemorative.put("22", "cn0206");
        mapOldNewCodes_Commemorative.put("23", "cn0207");
        mapOldNewCodes_Commemorative.put("24", "cn0208");
        mapOldNewCodes_Commemorative.put("25", "cn0209");
        mapOldNewCodes_Commemorative.put("27", "cn0211");
        mapOldNewCodes_Commemorative.put("26", "cn0210");
        mapOldNewCodes_Commemorative.put("28", "cn0212");
        mapOldNewCodes_Commemorative.put("29", "cn0213");
        mapOldNewCodes_Commemorative.put("30", "cn0214");
        mapOldNewCodes_Commemorative.put("31", "cn0215");
        mapOldNewCodes_Commemorative.put("32", "cn0216");
        mapOldNewCodes_Commemorative.put("33", "cn0217");
        mapOldNewCodes_Commemorative.put("34", "cn0218");
        mapOldNewCodes_Commemorative.put("33", "cn0219");
        mapOldNewCodes_Commemorative.put("36", "cn0220");
        mapOldNewCodes_Commemorative.put("33", "cn0221");
        mapOldNewCodes_Commemorative.put("38", "cn0222");
        mapOldNewCodes_Commemorative.put("39", "cn0223");
        mapOldNewCodes_Commemorative.put("40", "cn0224");
        mapOldNewCodes_Commemorative.put("41", "cn0225");
        mapOldNewCodes_Commemorative.put("42", "cn0226");
        mapOldNewCodes_Commemorative.put("43", "cn0227");
        mapOldNewCodes_Commemorative.put("41", "cn0228");
        mapOldNewCodes_Commemorative.put("45", "cn0229");
        mapOldNewCodes_Commemorative.put("46", "cn0230");
        mapOldNewCodes_Commemorative.put("47", "cn0231");
        mapOldNewCodes_Commemorative.put("48", "cn0232");
        mapOldNewCodes_Commemorative.put("49", "cn0233");
        mapOldNewCodes_Commemorative.put("50", "cn0234");
        mapOldNewCodes_Commemorative.put("51", "cn0235");
        mapOldNewCodes_Commemorative.put("52", "cn0236");
        mapOldNewCodes_Commemorative.put("53", "cn0237");
        mapOldNewCodes_Commemorative.put("54", "cn0238");
        mapOldNewCodes_Commemorative.put("55", "cn0239");
        mapOldNewCodes_Commemorative.put("56", "cn0240");
        mapOldNewCodes_Commemorative.put("57", "cn0241");
        mapOldNewCodes_Commemorative.put("58", "cn0242");
        mapOldNewCodes_Commemorative.put("59", "cn0243");
        mapOldNewCodes_Commemorative.put("60", "cn0244");
        mapOldNewCodes_Commemorative.put("61", "cn0245");
        mapOldNewCodes_Commemorative.put("62", "cn0246");
        mapOldNewCodes_Commemorative.put("63", "cn0247");
        mapOldNewCodes_Commemorative.put("64", "cn0248");
        mapOldNewCodes_Commemorative.put("65", "cn0249");
        mapOldNewCodes_Commemorative.put("66", "cn0250");
        mapOldNewCodes_Commemorative.put("67", "cn0251");
        mapOldNewCodes_Commemorative.put("68", "cn0252");
        mapOldNewCodes_Commemorative.put("69", "cn0253");
        mapOldNewCodes_Commemorative.put("70", "cn0254");
        mapOldNewCodes_Commemorative.put("71", "cn0255");
        mapOldNewCodes_Commemorative.put("72", "cn0256");
        mapOldNewCodes_Commemorative.put("73", "cn0257");
        mapOldNewCodes_Commemorative.put("74", "cn0258");
        mapOldNewCodes_Commemorative.put("75", "cn0259");
        mapOldNewCodes_Commemorative.put("76", "cn0260");
        mapOldNewCodes_Commemorative.put("77", "cn0261");
        mapOldNewCodes_Commemorative.put("78", "cn0262");
        mapOldNewCodes_Commemorative.put("79", "cn0263");
        mapOldNewCodes_Commemorative.put("80", "cn0264");
        mapOldNewCodes_Commemorative.put("81", "cn0265");
        mapOldNewCodes_Commemorative.put("82", "cn0266");
        mapOldNewCodes_Commemorative.put("83", "cn0267");
        mapOldNewCodes_Commemorative.put("84", "cn0268");
        mapOldNewCodes_Commemorative.put("85", "cn0269");
        mapOldNewCodes_Commemorative.put("86", "cn0270");
        mapOldNewCodes_Commemorative.put("87", "cn0271");
        mapOldNewCodes_Commemorative.put("94", "cn0272");
        mapOldNewCodes_Commemorative.put("88", "cn0273");
        mapOldNewCodes_Commemorative.put("89", "cn0274");
        mapOldNewCodes_Commemorative.put("90", "cn0275");
        mapOldNewCodes_Commemorative.put("86", "cn0276");
        mapOldNewCodes_Commemorative.put("92", "cn0277");
        mapOldNewCodes_Commemorative.put("93", "cn0278");
        mapOldNewCodes_Commemorative.put("97", "cn0279");
        mapOldNewCodes_Commemorative.put("98", "cn0280");
        mapOldNewCodes_Commemorative.put("95", "cn0281");
        mapOldNewCodes_Commemorative.put("96", "cn0282");
        mapOldNewCodes_Commemorative.put("99", "cn0283");
        mapOldNewCodes_Commemorative.put("100", "cn0284");
        mapOldNewCodes_Commemorative.put("101", "cn0285");
        mapOldNewCodes_Commemorative.put("102", "cn0286");

    }

    /* FIM - Deal with old files */
}