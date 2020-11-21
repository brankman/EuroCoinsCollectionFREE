package moedas.euro.free;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

public class CountriesCoinsActivity extends AppCompatActivity {

    static Database mDataBase;
    Functions classFunctions = new Functions(getApplicationContext());
    HashSet<String> arrayIdCoinsChecked = new HashSet<>();
    HashSet<String> arrayIdCoinsUnChecked = new HashSet<>();
    HashSet<String> arrayIdCoins = new HashSet<>();
    CustomCursorAdapter customAdapter;
    private int selectAll = 0;
    private static int country_num = 0;
    private static int countTotal_coin_state = 0;
    private static int totalCoinsPerCountry = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries_coins);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Start ads**/
        classFunctions.startAds();

        ListView listViewBase = findViewById(R.id.lv_coins);

        mDataBase = new Database(this);
        try {
            mDataBase.open();

            country_num = (int) getIntent().getExtras().getLong("SelectedCountryIndex");
            /*INICIO - monta a imagem e o titulo da activity*/
            Cursor c = mDataBase.selectCountryCoinsHave(country_num);
            if (c.moveToFirst()) {

                byte[] bb = c.getBlob(c.getColumnIndex("country_flag"));
                if (bb != null) {
                    @SuppressWarnings("deprecation")
                    Drawable d = new BitmapDrawable(BitmapFactory.decodeByteArray(bb, 0, bb.length));
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    getSupportActionBar().setIcon(d);
                }

                String country_name = c.getString(c.getColumnIndex("country_name"));
                if(country_name != null)
                {
                    int resId = getResources().getIdentifier(country_name, "string", getPackageName());
                    getSupportActionBar().setTitle("  " + getString(resId));
                }

                do {
                    if(c.getInt(c.getColumnIndex("coin_state")) == 1)
                    {
                        countTotal_coin_state = countTotal_coin_state + 1;
                    }
                }while(c.moveToNext());
            }
            /*FIM - monta a imagem e o titulo da activity*/

            /*INICIO - calcula o numero maximo de moedas*/
            Cursor cursorCountCoinsPerCountry = mDataBase.selectCountCoinsPerCountry(country_num);
            if (cursorCountCoinsPerCountry.moveToFirst()) {
                totalCoinsPerCountry = cursorCountCoinsPerCountry.getInt(cursorCountCoinsPerCountry.getColumnIndex("totalCoin_Country"));;
            }
            /*FIM - calcula o numero maximo de moedas*/

            listViewBase.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                    try {
                        /*INICIO - monta dados para a dialog com imagem grande*/
                        mDataBase.open();

                        Cursor cursor = mDataBase.selectCountryCoinDataDialog(arg3);
                        if (cursor.moveToFirst()) {

                            Dialog d = new Dialog(CountriesCoinsActivity.this);
                            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            d.setContentView(R.layout.dialog_custom_simple);
                            TextView dialog_title = d.findViewById(R.id.tv_dialogTitle);
                            dialog_title.setText(cursor.getString(cursor.getColumnIndex("coin_value")));

                            byte[] bb = cursor.getBlob(cursor.getColumnIndex("coin_image"));
                            if (bb != null) {
                                ImageView image = d.findViewById(R.id.iv_dialog_coin);
                                image.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));
                            }

                            d.show();
                            /*FIM - monta dados para a dialog com imagem grande*/
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    finally {
                        mDataBase.close();
                    }
                }
            });

            customAdapter = new CustomCursorAdapter(this, c);
            listViewBase.setAdapter(customAdapter);
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
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
                i = new Intent(getApplicationContext(), CountriesActivity.class);
                i.putExtra("isBack", true);
                startActivity(i);
                finish();
                break;
            case R.id.menu_select_deselect_all:
                //1 = true   |   2 = false
                if(arrayIdCoinsChecked.size() == totalCoinsPerCountry || countTotal_coin_state >= totalCoinsPerCountry)
                { selectAll = 2; countTotal_coin_state = 0;}
                else
                { selectAll = 1; countTotal_coin_state = totalCoinsPerCountry; }
                customAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_item_save:
                try {
                    mDataBase.open();
                    if(selectAll == 1)
                    {
                        mDataBase.editAllCoins(country_num, 1);
                    }
                    else if(selectAll == 2)
                    {
                        mDataBase.editAllCoins(country_num, 0);
                    }
                    else
                    {
                        for (String item2 : arrayIdCoins) {
                            if (arrayIdCoinsChecked.contains(item2)) {
                                mDataBase.editCoins(item2, 1);
                            } else if (arrayIdCoinsUnChecked.contains(item2)) {
                                mDataBase.editCoins(item2, 0);
                            }
                        }
                    }

                    countTotal_coin_state = 0;
                    arrayIdCoins.clear();
                    arrayIdCoinsChecked.clear();
                    arrayIdCoinsUnChecked.clear();
                    mDataBase.close();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                finally {
                    i = new Intent(getApplicationContext(), CountriesActivity.class);
                    i.putExtra("isBack", true);
                    startActivity(i);
                    finish();
                    Toast.makeText(getApplicationContext(), R.string.toast_data_stored, Toast.LENGTH_SHORT).show();
                }

                break;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        arrayIdCoins.clear();
        arrayIdCoinsChecked.clear();
        arrayIdCoinsUnChecked.clear();
        Intent i = new Intent(getApplicationContext(), CountriesActivity.class);
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

            byte[] blobCoinImg = cursor.getBlob(cursor.getColumnIndex("coin_image"));
            if (blobCoinImg != null) {
                ImageView image = view.findViewById(R.id.iv_icon_coin);
                image.setImageBitmap(BitmapFactory.decodeByteArray(blobCoinImg, 0, blobCoinImg.length));
            }

            TextView title = view.findViewById(R.id.tv_title_coin);
            final String coin_country = cursor.getString(cursor.getColumnIndex("coin_value"));
            title.setText(coin_country);

            CheckBox cbCoinState = view.findViewById(R.id.cb_coin_state);
            final String coin_code = cursor.getString(cursor.getColumnIndex("coin_code"));
            cbCoinState.setTag(coin_code);
            arrayIdCoins.add(coin_code);


            if(arrayIdCoinsChecked.contains(cursor.getString(cursor.getColumnIndex("coin_code")))){
                cbCoinState.setChecked(true);
            }
            else if(arrayIdCoinsUnChecked.contains(cursor.getString(cursor.getColumnIndex("coin_code")))){
                cbCoinState.setChecked(false);
            }
            else {
                if(cursor.getInt(cursor.getColumnIndex("coin_state")) == 1){
                    cbCoinState.setChecked(true);
                    arrayIdCoinsUnChecked.remove(cursor.getString(cursor.getColumnIndex("coin_code")));
                    arrayIdCoinsChecked.add(cursor.getString(cursor.getColumnIndex("coin_code")));
                }else if(cursor.getInt(cursor.getColumnIndex("coin_state")) == 0){
                    cbCoinState.setChecked(false);
                    arrayIdCoinsChecked.remove(cursor.getString(cursor.getColumnIndex("coin_code")));
                    arrayIdCoinsUnChecked.add(cursor.getString(cursor.getColumnIndex("coin_code")));
                }
            }

            cbCoinState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    if(arg1){
                        arrayIdCoinsUnChecked.remove(arg0.getTag().toString());
                        arrayIdCoinsChecked.add(arg0.getTag().toString());

                    }else{
                        arrayIdCoinsChecked.remove(arg0.getTag().toString());
                        arrayIdCoinsUnChecked.add(arg0.getTag().toString());
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