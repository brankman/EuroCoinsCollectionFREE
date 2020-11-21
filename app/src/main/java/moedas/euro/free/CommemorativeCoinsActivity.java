package moedas.euro.free;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

import java.util.HashSet;

public class CommemorativeCoinsActivity extends AppCompatActivity {

    static Database mDataBase;
    Functions classFunctions = new Functions(getApplicationContext());
    HashSet<String> arrayIdChecked = new HashSet<String>();
    HashSet<String> arrayIdunChecked = new HashSet<String>();
    HashSet<String> arrayId = new HashSet<String>();
    CustomCursorAdapter customAdapter;
    private int selectAll = 0;
    private static Long commemorativeParentSelected;
    private static int countTotal_coin_state = 0;
    private static int totalCoinsPerYear = 0;
    static int sortMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commemorative_coins);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Start ads**/
        classFunctions.startAds();

        ListView listViewBase = findViewById(R.id.listViewComemoratives);

        mDataBase = new Database(this);
        try {
            mDataBase.open();

            commemorativeParentSelected = getIntent().getExtras().getLong("selectedItem");
            sortMode = getIntent().getExtras().getInt("sortMode");
            Cursor c;
            if(sortMode == 0)
            {
                setTitle(commemorativeParentSelected.toString());

                /*INICIO - calcula o numero maximo de moedas*/
                Cursor cursorCountCoinsPerYear = mDataBase.selectCountCoinsPerYear(commemorativeParentSelected);
                if (cursorCountCoinsPerYear.moveToFirst()) {
                    totalCoinsPerYear = cursorCountCoinsPerYear.getInt(cursorCountCoinsPerYear.getColumnIndex("totalCoin_Year"));;
                }
                /*FIM - calcula o numero maximo de moedas*/

                c = mDataBase.selectMoedasComemorativasTenho(commemorativeParentSelected);
            }
            else if(sortMode == 1)
            {
                /*INICIO - calcula o numero maximo de moedas*/
                Cursor cursorCountCoinsPerCountry = mDataBase.selectCountCoinsPerCountry(commemorativeParentSelected);
                if (cursorCountCoinsPerCountry.moveToFirst()) {
                    totalCoinsPerYear = cursorCountCoinsPerCountry.getInt(cursorCountCoinsPerCountry.getColumnIndex("totalCoin_Country"));;
                }
                /*FIM - calcula o numero maximo de moedas*/

                Cursor crs = mDataBase.selectCountryByNum(commemorativeParentSelected);
                if (crs.moveToFirst()) {
                    String country_name = crs.getString(crs.getColumnIndex("country_name"));
                    if (country_name != null) {
                        int resId = getResources().getIdentifier(country_name, "string", getPackageName());
                        setTitle(getString(resId));
                    }
                }
                c = mDataBase.selectMoedasComemorativasCountryTenho(commemorativeParentSelected);
            }
            else
            { throw new Exception(); }


            listViewBase.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    try {
                        mDataBase.open();

                        Cursor cursor = mDataBase.selectMoedasComemorativasDialog(arg3);
                        if(cursor.moveToFirst()){

                            Dialog d = new Dialog(CommemorativeCoinsActivity.this);
                            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            d.setContentView(R.layout.dialog_custom_simple_description);


                            TextView dialog_title = d.findViewById(R.id.tv_dialogTitle);
                            /** Titles **/
                            switch (sortMode){
                                case 0:
                                    String country_name = cursor.getString(cursor.getColumnIndex("country_name"));
                                    dialog_title.setText(
                                            country_name != null
                                            ? getString( getResources().getIdentifier(country_name, "string", getPackageName()) )
                                            : ""
                                    );
                                    break;
                                case 1:
                                    dialog_title.setText(cursor.getString(cursor.getColumnIndex("coin_year")));
                                    break;
                                default:
                                    dialog_title.setText("");
                            }

                            byte[] bb = cursor.getBlob(cursor.getColumnIndex("coin_image"));
                            if (bb != null) {
                                ImageView image = d.findViewById(R.id.iv_dialog_coin);
                                image.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));
                            }

                            TextView tv_dialog = d.findViewById(R.id.tv_dialog_about);
                            /** Descriptions **/
                            String country_description = cursor.getString(cursor.getColumnIndex("coin_description"));
                            tv_dialog.setText(
                                    country_description != null
                                    ? getString( getResources().getIdentifier(country_description, "string", getPackageName()) )
                                    : ""
                            );

                            d.show();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    finally {
                        mDataBase.close();
                    }
                }
            });

            if (c.moveToFirst()) {
                do {
                    if(c.getInt(c.getColumnIndex("coin_state")) == 1)
                    {
                        countTotal_coin_state = countTotal_coin_state + 1;
                    }
                }while(c.moveToNext());
            }

            customAdapter = new CustomCursorAdapter(this, c);
            listViewBase.setAdapter(customAdapter);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_coins, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                i = new Intent(getApplicationContext(), CommemorativeYearsActivity.class);
                i.putExtra("isBack", true);
                startActivity(i);
                finish();
                break;
            case R.id.menu_select_deselect_all:
                //1 = true   |   2 = false
                if(arrayIdChecked.size() == totalCoinsPerYear || countTotal_coin_state >= totalCoinsPerYear)
                { selectAll = 2; countTotal_coin_state = 0;}
                else
                { selectAll = 1; countTotal_coin_state = totalCoinsPerYear; }
                customAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_item_save:
                try {
                    mDataBase.open();
                    if(selectAll == 1)
                    {
                        if(sortMode == 0)
                        { mDataBase.editAllCommemorativeCoinsPerYear(commemorativeParentSelected, 1); }
                        else if(sortMode == 1)
                        { mDataBase.editAllCommemorativeCoinsPerCountry(commemorativeParentSelected, 1); }
                    }
                    else if(selectAll == 2)
                    {
                        if(sortMode == 0)
                        { mDataBase.editAllCommemorativeCoinsPerYear(commemorativeParentSelected, 0); }
                        else if(sortMode == 1)
                        { mDataBase.editAllCommemorativeCoinsPerCountry(commemorativeParentSelected, 0); }
                    }
                    else
                    {
                        for (String item2 : arrayId) {
                            if (arrayIdChecked.contains(item2)) {
                                mDataBase.editCoins(item2, 1);
                            } else if (arrayIdunChecked.contains(item2)) {
                                mDataBase.editCoins(item2, 0);
                            }
                        }
                    }

                    countTotal_coin_state = 0;
                    arrayId.clear();
                    arrayIdChecked.clear();
                    arrayIdunChecked.clear();
                    mDataBase.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                i = new Intent(getApplicationContext(), CommemorativeYearsActivity.class);
                i.putExtra("isBack", true);
                startActivity(i);
                finish();
                Toast.makeText(getApplicationContext(), R.string.toast_data_stored, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        arrayId.clear();
        arrayIdChecked.clear();
        arrayIdunChecked.clear();
        Intent i = new Intent(getApplicationContext(), CommemorativeYearsActivity.class);
        i.putExtra("isBack", true);
        startActivity(i);
        finish();
    }


    private class CustomCursorAdapter extends CursorAdapter {

        @SuppressWarnings("deprecation")
        public CustomCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            byte[] bb = cursor.getBlob(cursor.getColumnIndex("coin_image"));
            if (bb != null) {
                ImageView image = (ImageView) view.findViewById(R.id.iv_icon_coin);
                image.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));
            }


            TextView title = (TextView) view.findViewById(R.id.tv_title_coin);
            switch (sortMode){
                case 0:
                    String coin_country = cursor.getString(cursor.getColumnIndex("country_name"));
                    title.setText(
                            coin_country != null
                                    ? getString( getResources().getIdentifier(coin_country, "string", getPackageName()) )
                                    : ""
                    );
                    break;
                case 1:
                    title.setText(cursor.getString(cursor.getColumnIndex("coin_year")));
                    break;
                default:
                    title.setText("");
            }


            CheckBox cbCoinState = (CheckBox) view.findViewById(R.id.cb_coin_state);
            final String coin_code = cursor.getString(cursor.getColumnIndex("coin_code"));
            cbCoinState.setTag(coin_code);
            arrayId.add(coin_code);

            if(arrayIdChecked.contains(cursor.getString(cursor.getColumnIndex("coin_code")))){
                cbCoinState.setChecked(true);
            }
            else if(arrayIdunChecked.contains(cursor.getString(cursor.getColumnIndex("coin_code")))){
                cbCoinState.setChecked(false);
            }
            else {
                if(cursor.getInt(cursor.getColumnIndex("coin_state")) == 1){
                    cbCoinState.setChecked(true);
                    arrayIdunChecked.remove(cursor.getString(cursor.getColumnIndex("coin_code")));
                    arrayIdChecked.add(cursor.getString(cursor.getColumnIndex("coin_code")));
                }else if(cursor.getInt(cursor.getColumnIndex("coin_state")) == 0){
                    cbCoinState.setChecked(false);
                    arrayIdChecked.remove(cursor.getString(cursor.getColumnIndex("coin_code")));
                    arrayIdunChecked.add(cursor.getString(cursor.getColumnIndex("coin_code")));
                }
            }

            cbCoinState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    if(arg1){
                        arrayIdunChecked.remove(arg0.getTag().toString());
                        arrayIdChecked.add(arg0.getTag().toString());
                    }else{
                        arrayIdChecked.remove(arg0.getTag().toString());
                        arrayIdunChecked.add(arg0.getTag().toString());
                    }
                }
            });

            if(selectAll == 1)
                cbCoinState.setChecked(true);
            else if(selectAll == 2)
                cbCoinState.setChecked(false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View retView = inflater.inflate(R.layout.list_custom_simple_checkbox, parent, false);

            return retView;
        }
    }
}
