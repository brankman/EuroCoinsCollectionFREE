package moedas.euro.free;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.legacy.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    static Database mDataBase;
    //Permission code that will be checked in the method onRequestPermissionsResult
    private int STORAGE_PERMISSION_CODE = 1000;
    Functions classFunctions = new Functions(this);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(!isReadStorageAllowed()){
            //If the app has not the permission then asking for the permission
            requestStoragePermission();
        }
        //else {
        //    //If permission is already having then showing the toast
        //    Toast.makeText(MainActivity.this,"You already have the permission", Toast.LENGTH_LONG).show();
        //}


        /** Start ads**/
        classFunctions.startAds();

        /**evento do botao para aceder às moedas "normais"**/
        Button bt_euro_coins = findViewById(R.id.bt_go_countries);
        bt_euro_coins.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CountriesActivity.class);
                i.putExtra("isBack", false);
                startActivity(i);
            }
        });

        /**evento do botao para aceder às moedas comemorativas**/
        Button bt_commemorative_euro_coins = findViewById(R.id.bt_go_commemorative_years);
        bt_commemorative_euro_coins.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CommemorativeYearsActivity.class);
                i.putExtra("isBack", false);
                startActivity(i);
            }
        });


        showRateMeDialog();

        manageDBInitializationBackuo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_statistics:
                startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                this.finish();
                break;
            case R.id.menu_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
        }
        return false;
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }



    //We are calling this method to check the permission status
    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission(){

        //if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        //}

        //And finally ask for the permission
        //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            //performAction(...);
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            //showInContextUI(...);
        } else {
            // You can directly ask for the permission.
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, STORAGE_PERMISSION_CODE);

        }
    }

    private void showRateMeDialog(){

        /**algoritmo criado de forma completamente aleatória
         * para mostrar a dialog a pedir para avaliar a app**/
        if(((int)((1.5 + Math.random()) * 7.2)) % 3 == 0)
        {
            Dialog d = new Dialog(MainActivity.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.dialog_custom_rate_app);
            d.show();

            Button bt_rate_it = d.findViewById(R.id.bt_dialog_rate_it);
            bt_rate_it.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moedas.euro.free")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moedas.euro.free&feature=search_result#?t=W251bGwsMSwxLDEsIm1vZWRhcy5ldXJvLmZyZWUiXQ..")));
                    }
                }
            });
        }
    }

    private void manageDBInitializationBackuo()
    {
        File dbFile = this.getDatabasePath("BDEuro");
        long dbSize = dbFile.length();


        /**Cria pasta na memoria do telemovel para meter os ficheiros de backup**/
////        String moedas = "EuroCoinsCollection";
        final File moedasDir = new File(Environment.getExternalStorageDirectory(), "EuroCoinsCollection");

        if(!moedasDir.exists())
        {
            moedasDir.mkdirs();
        }

        /*REVER ESTE MECANISMO. Já funcionam os backups, mas a logica de ir buscar a BD vazia ou não ainda não está bem*/
        if(dbSize < 18617344)
        {
            Thread loadThread = new Thread(){
                public void run(){
                    try{

                        //String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                        String extStorageDirectory = getApplicationContext().getExternalFilesDir(null).toString();

                        //cria back-up
                        classFunctions.writeToCSV();

                        //import DB nova
                        classFunctions.importInitialDB();

                        //importa o back-up criado
                        //File myFile = new File(extStorageDirectory + "/EuroCoinsCollection/coinsBackUp.csv");
                        File myFile = new File(extStorageDirectory + "/coinsBackUp.csv");

                        if(myFile.exists())
                        {
                            //classFunctions.readFromCSV(extStorageDirectory + "/EuroCoinsCollection/coinsBackUp.csv");
                            classFunctions.readFromCSV(extStorageDirectory + "/coinsBackUp.csv");
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            loadThread.start();
        }
    }

}
