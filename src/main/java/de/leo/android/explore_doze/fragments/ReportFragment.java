package de.leo.android.explore_doze.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.leo.android.explore_doze.util.LogActions;
import de.leo.android.explore_doze.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {


    public ReportFragment() {
        // Required empty public constructor
    }

    private TextView tvResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        tvResult = (TextView)v.findViewById(R.id.tvReport);

        Button b = (Button)v.findViewById(R.id.btnReport);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvResult.setText(LogActions.checkEvents(getContext(), null));
            }
        });
        return v;
    }

}
