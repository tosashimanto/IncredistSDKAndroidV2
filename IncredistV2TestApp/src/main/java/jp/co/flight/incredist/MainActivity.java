package jp.co.flight.incredist;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jp.co.flight.incredist.databinding.ActivityMainBinding;
import jp.co.flight.incredist.model.IncredistModel;

public class MainActivity extends AppCompatActivity {

    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        IncredistModel model = new IncredistModel.Impl(this);
        mPresenter = new MainPresenter.Impl(binding, model);

        binding.setPresenter(mPresenter);
    }
}
