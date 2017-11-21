package jp.co.flight.incredist;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import jp.co.flight.incredist.android.IncredistV2TestApp.R;
import jp.co.flight.incredist.model.IncredistModel;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener {

    private static final String FRAGMENT_TAG_MAIN = "fragment_tag_main";

    private IncredistModel.Impl mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mModel = new IncredistModel.Impl(this);

        MainFragment fragment = MainFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.container, fragment, FRAGMENT_TAG_MAIN);
        ft.commit();
    }

    @Override
    public IncredistModel getModel() {
        return mModel;
    }
}
