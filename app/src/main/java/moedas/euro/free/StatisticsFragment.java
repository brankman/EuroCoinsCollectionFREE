package moedas.euro.free;


import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cursoradapter.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {

    static Database mDataBase;

    public StatisticsFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_statistics, container, false);

        Bundle fragArgument = getArguments();

        mDataBase = new Database(getActivity());
        try {
            mDataBase.open();

            ListView lv_stats = v.findViewById(R.id.list_statistics);

            if(fragArgument.getInt("tab1") == 1)
            {
                Cursor c = mDataBase.selectCountryCoins();
                lv_stats.setAdapter(new CustomCursorAdapterCoinsCountry(getActivity(), c));
            }
            else if(fragArgument.getInt("tab2") == 2)
            {
                Cursor c = mDataBase.selectMoedasComemorativasAno();
                lv_stats.setAdapter(new CustomCursorAdapterCommemorativeCoins(getActivity(), c));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            mDataBase.close();
        }

        return v;
    }





    private static class CustomCursorAdapterCoinsCountry extends CursorAdapter {

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @SuppressWarnings("deprecation")
        public CustomCursorAdapterCoinsCountry(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            byte[] bb = cursor.getBlob(cursor.getColumnIndex("country_flag"));
            if (bb != null) {
                ImageView imgV_stats_tab1_flags = view.findViewById(R.id.imgV_stats_tab1_flags);
                imgV_stats_tab1_flags.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));
            }

            TextView tv_stats_tab1_country_name = view.findViewById(R.id.tv_stats_tab1_country_name);
            String country_name = cursor.getString(cursor.getColumnIndex("country_name"));

            if(country_name != null)
            {
                int resId = context.getResources().getIdentifier(country_name, "string", "moedas.euro.free");
                tv_stats_tab1_country_name.setText(context.getString(resId));
            }

            try {
                mDataBase.open();
                Cursor crs = mDataBase.selectCountryCoinsCount(country_name);
                if (crs.moveToFirst()) {
                    int count = crs.getInt(crs.getColumnIndex("totalCoinsCountry"));

                    ProgressBar progrBar_stats_tab1 = view.findViewById(R.id.progrBar_stats_tab1);
                    progrBar_stats_tab1.setProgress(count * 100 / 8);

                    TextView tv_stats_tab1_percent = view.findViewById(R.id.tv_stats_tab1_percent);
                    tv_stats_tab1_percent.setText(count * 100 / 8 + "%");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }finally {
                mDataBase.close();
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View retView = inflater.inflate(R.layout.list_custom_statistics, parent, false);
            return retView;
        }
    }


    private static class CustomCursorAdapterCommemorativeCoins extends CursorAdapter {
        @Override
        public boolean isEnabled(int position) {
            // TODO Auto-generated method stub
            return false;
        }
        @SuppressWarnings("deprecation")
        public CustomCursorAdapterCommemorativeCoins(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView tv_stats_tab1_country_name = view.findViewById(R.id.tv_stats_tab1_country_name);
            String year = cursor.getString(cursor.getColumnIndex("_id"));
            tv_stats_tab1_country_name.setText(year);

            try {
                mDataBase.open();
                Cursor cursor_totalCount = mDataBase.selectMoedasComemorativasCount(year);
                Cursor cursor_tenhoCount = mDataBase.selectMoedasComemorativasCountTenho(year);
                if (cursor_totalCount.moveToFirst() && cursor_tenhoCount.moveToFirst()) {
                    int totalCount = cursor_totalCount.getInt(cursor_totalCount.getColumnIndex("totalCom"));
                    int tenhoCount = cursor_tenhoCount.getInt(cursor_tenhoCount.getColumnIndex("totalComTenho"));

                    ProgressBar progrBar_stats_tab1 = view.findViewById(R.id.progrBar_stats_tab1);
                    progrBar_stats_tab1.setProgress(tenhoCount * 100 / totalCount);

                    TextView tv_stats_tab1_percent = view.findViewById(R.id.tv_stats_tab1_percent);
                    tv_stats_tab1_percent.setText(tenhoCount * 100 / totalCount + "%");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }finally {
                mDataBase.close();
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View retView = inflater.inflate(R.layout.list_custom_statistics, parent, false);
            return retView;
        }
    }

}
