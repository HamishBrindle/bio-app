package com.biomap.application.bio_app.Mapping;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.biomap.application.bio_app.R;

/**
 * Created by hamis on 2017-06-13.
 */

public class MappingFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapping, container, false);
    }
}
