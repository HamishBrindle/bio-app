package com.example.hamishbrindle.bio_app.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hamishbrindle.bio_app.R;

/**
 * Created by hamis on 2017-06-13.
 */

public class MenuFragment extends Fragment {

    private static final String TAG = "MenuFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        return view;
    }
}
