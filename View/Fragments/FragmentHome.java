package de.hsf.mobcomgroup1.runourway.View.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.hsf.mobcomgroup1.runourway.R;
import de.hsf.mobcomgroup1.runourway.View.MainActivity;
import de.hsf.mobcomgroup1.runourway.View.SecondActivity;

public class FragmentHome extends Fragment {

    private static final String TAG = "FragmentHome";

    private Button navGenBtn;
    private Button navCusBtn;
    private Button navStatBtn;

    public static boolean gen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment_layout, container, false);

        navGenBtn = (Button) view.findViewById(R.id.navGenBtn);
        navCusBtn = (Button) view.findViewById(R.id.navCusBtn);
        navStatBtn = (Button) view.findViewById(R.id.navStatBtn);

        navGenBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                gen = true;
                ((MainActivity)getActivity()).isCusSet=false;
                Intent intent = new Intent(getActivity(), SecondActivity.class);
                startActivity(intent);
            }
        });
        navCusBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                gen = false;
                ((MainActivity)getActivity()).isCusSet=true;
                Intent intent = new Intent(getActivity(), SecondActivity.class);

                startActivity(intent);

            }
        });
        navStatBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setViewPager(1);
            }
        });
        return view;
    }
}
