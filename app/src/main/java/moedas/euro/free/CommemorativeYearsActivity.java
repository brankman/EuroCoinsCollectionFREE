package moedas.euro.free;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import androidx.core.app.NavUtils;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

public class CommemorativeYearsActivity extends AppCompatActivity {

    static Database mDataBase;
    Functions classFunctions = new Functions(getApplicationContext());
    static long selected_index = 0;
    ListView lv;
    static int listView_index = 0;
    static int sortMode = 0;
    CustomCursorAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commemorative_years);
        getSupportActionBar().setTitle(R.string.title_commemoratives);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Start ads**/
        classFunctions.startAds();

        lv = findViewById(R.id.lv_special);

        mDataBase = new Database(this);
        try {
            mDataBase.open();
            PopulaListView(sortMode);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toggle_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(getApplicationContext(), MainActivity.class));
                listView_index = 0;
                break;
            case R.id.menu_sortPerYear:
                sortMode = 0;
                try {
                    mDataBase.open();
                    PopulaListView(sortMode);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                finally {
                    mDataBase.close();
                }
                break;
            case R.id.menu_sortPerCountry:
                sortMode = 1;
                try {
                    mDataBase.open();
                    PopulaListView(sortMode);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                finally {
                    mDataBase.close();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*este método permite guardar a posição da listView quando se sai deste ecrã*/
    @Override
    protected void onPause() {
        super.onPause();
        listView_index = lv.getFirstVisiblePosition();
    }

    private class CustomCursorAdapter extends CursorAdapter {

        @SuppressWarnings("deprecation")
        public CustomCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            switch (sortMode) {
                case 0: //Years
                    TextView year = view.findViewById(R.id.tv_title_name);
                    String year_value = cursor.getString(cursor.getColumnIndex("_id"));
                    year.setText(year_value);

                    try {
                        mDataBase.open();
                        Cursor totalCommemorativeCoins = mDataBase.selectMoedasComemorativasCount(year_value);
                        Cursor haveCommemorativeCoins = mDataBase.selectMoedasComemorativasCountTenho(year_value);

                        if (totalCommemorativeCoins.moveToFirst() && haveCommemorativeCoins.moveToFirst()) {
                            int totalCommemorativeCoins_value = totalCommemorativeCoins.getInt(totalCommemorativeCoins.getColumnIndex("totalCom"));
                            int haveCommemorativeCoins_value = haveCommemorativeCoins.getInt(haveCommemorativeCoins.getColumnIndex("totalComTenho"));

                            TextView stat = view.findViewById(R.id.tv_subtitle_stats);
                            stat.setText(getString(R.string.subtitleList) + " " + haveCommemorativeCoins_value + "/" + totalCommemorativeCoins_value);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }finally {
                        mDataBase.close();
                    }

                    break;
                case 1: //Countries
                    byte[] blobFlag = cursor.getBlob(cursor.getColumnIndex("country_flag"));
                    if (blobFlag != null) {
                        ImageView image = view.findViewById(R.id.iv_icon);
                        image.setImageBitmap(BitmapFactory.decodeByteArray(blobFlag, 0, blobFlag.length));
                    }

                    TextView itemTitle = view.findViewById(R.id.tv_title_name);
                    String country_name = cursor.getString(cursor.getColumnIndex("country_name"));

                    if(country_name != null)
                    {
                        int resId = context.getResources().getIdentifier(country_name, "string", getPackageName());
                        itemTitle.setText(context.getString(resId));
                    }

                    try {
                        mDataBase.open();
                        Cursor totalCommemorativeCoins = mDataBase.selectMoedasComemorativasCountryCount(country_name);
                        Cursor haveCommemorativeCoins = mDataBase.selectMoedasComemorativasCountryCountTenho(country_name);

                        if (totalCommemorativeCoins.moveToFirst() && haveCommemorativeCoins.moveToFirst()) {
                            int totalCommemorativeCoins_value = totalCommemorativeCoins.getInt(totalCommemorativeCoins.getColumnIndex("totalCoinsCommemorativeCountry"));
                            int haveCommemorativeCoins_value = haveCommemorativeCoins.getInt(haveCommemorativeCoins.getColumnIndex("totalComCountryTenho"));

                            TextView stat = view.findViewById(R.id.tv_subtitle_stats);
                            stat.setText(getString(R.string.subtitleList) + " " + haveCommemorativeCoins_value + "/" + totalCommemorativeCoins_value);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }finally {
                        mDataBase.close();
                    }
                    break;
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View retView = inflater.inflate(R.layout.list_custom_simple, parent, false);

            return retView;
        }
    }


    private void PopulaListView(final int sortMode)
    {
        Cursor c = null;
        if(sortMode == 0)
        { c = mDataBase.selectMoedasComemorativasAno(); }
        else if(sortMode == 1)
        { c = mDataBase.selectMoedasComemorativasPais(); }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selected_index=arg3;
                Intent i = new Intent(getApplicationContext(),CommemorativeCoinsActivity.class);
                i.putExtra("selectedItem", selected_index);
                i.putExtra("sortMode", sortMode);
                startActivity(i);
                finish();
            }
        });

        customAdapter = new CustomCursorAdapter(this, c);
        lv.setAdapter(customAdapter);

        /*este IF serve posicionar a lista no mesmo ponto quando voltamos novamente a este ecrã*/
        boolean isBack = getIntent().getExtras().getBoolean("isBack");
        if(listView_index != 0 && isBack)
            lv.setSelection(listView_index);
        else
            lv.setSelection(0);
    }
}
