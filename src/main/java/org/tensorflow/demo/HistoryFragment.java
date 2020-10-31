package org.tensorflow.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class HistoryFragment extends Fragment{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    PlateDbHelper dbHelper;



    ArrayList<Plate> plates;

    private OnFragmentInteractionListener mListener;



    public HistoryFragment() {

     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
  
        dbHelper = new PlateDbHelper(getActivity());
        plates = dbHelper.readPlateTexts();
        return createHistoryLayout(plates);

    }

  
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    
    public interface OnFragmentInteractionListener {
      
        void onFragmentInteraction(Uri uri);
    }



    public CardView createSearchView(){
        CardView card = new CardView(getActivity());
        final SearchView search = new SearchView(getActivity());
        search.setBackgroundColor(getResources().getColor(R.color.colorSearch));
        search.setIconifiedByDefault(false);
        search.setQueryHint("Search for a license plate");

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
 
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {



                LinearLayout initialConstraint = getView().findViewById(R.id.withText);
                LinearLayout wrapper = getView().findViewById(R.id.sawtooth);



                if (newText.length() > 0) {
                    wrapper.removeView(getView().findViewById(R.id.triangle));
                    initialConstraint.setVisibility(View.GONE);
                    LinearLayout searchLinearLayout = createSearchHistoryLayout(plates,newText);
                    wrapper.addView(searchLinearLayout);

                } else {
                    wrapper.removeView(getView().findViewById(R.id.triangle));
                    initialConstraint.setVisibility(View.VISIBLE);

                }

                return false;
            }
        });
        card.setId(View.generateViewId());

        card.addView(search);
        card.setRadius(10f);
        card.setCardElevation(8f);





        return card;
    }


    public ArrayList<Plate> filterPlates(ArrayList<Plate> plates, String newText){
        ArrayList<Plate> filteredPlates = new ArrayList<Plate>();

        for (Plate plate : plates){
            String existingLicense = plate.getText();
            String[] results = existingLicense.split(newText);
            if (results.length>1){
                filteredPlates.add(plate);
            }
        }

        return filteredPlates;
    }

    public LinearLayout createSearchHistoryLayout(ArrayList<Plate> plates, String newText){



        ArrayList<Plate> filteredPlates= filterPlates(plates,newText);


        LinearLayout innerLayout= createAllPlateEntries(filteredPlates);
        innerLayout.setId(R.id.triangle);



        return innerLayout;
    }



    public ConstraintLayout createHistoryLayout(ArrayList<Plate> plates){


        ConstraintLayout constraint = new ConstraintLayout(getActivity());
        constraint.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        constraint.setLayoutParams(lparams);



        ConstraintSet set = new ConstraintSet();



        CardView search = createSearchView();
        constraint.addView(search);






        ScrollView scroller = new ScrollView(getActivity());
        scroller.setId(View.generateViewId());







        LinearLayout innerConstraint = createAllPlateEntries(plates);
        innerConstraint.setId(R.id.withText);

        LinearLayout wrapperLayout = new LinearLayout(getActivity());
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);
        wrapperLayout.setId(R.id.sawtooth);
        wrapperLayout.addView(innerConstraint);



        scroller.addView(wrapperLayout);
        constraint.addView(scroller);
        set.constrainHeight(scroller.getId(), ConstraintSet.WRAP_CONTENT);
        set.constrainWidth(scroller.getId(), ConstraintSet.MATCH_CONSTRAINT);

        int px60 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, this.getResources().getDisplayMetrics());
        set.connect(scroller.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, px60);
        set.connect(scroller.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, this.getResources().getDisplayMetrics());
        set.connect(scroller.getId(), ConstraintSet.TOP, search.getId(), ConstraintSet.BOTTOM, px);





        int px50 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, this.getResources().getDisplayMetrics());
        set.constrainHeight(search.getId(), px50);
        set.constrainWidth(search.getId(), ConstraintSet.MATCH_CONSTRAINT);
        int px25 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, this.getResources().getDisplayMetrics());
        set.connect(search.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, px25);
        int px30 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, this.getResources().getDisplayMetrics());
        set.connect(search.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, px30);
        int px100 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, this.getResources().getDisplayMetrics());
        set.connect(search.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 100);



        set.applyTo(constraint);
        return constraint;

    }


    public TextView createPlateIDEntry(Plate plate){
        LinearLayout.LayoutParams linearparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        String id = plate.get_ID();
        final TextView id_text = new TextView(getActivity());
        id_text.setLayoutParams(linearparams);
        id_text.setText(id);
        id_text.setVisibility(View.GONE);
        id_text.setId(View.generateViewId());
        return id_text;
    }

    public ConstraintLayout createHistoryEntry(Plate plate){

        ConstraintLayout.LayoutParams linearparams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);


        final TextView plateText = new TextView(getActivity());
    
        plateText.setText("" + plate.getText());



    
        plateText.setTextColor(getResources().getColor(R.color.black));
     
        int px15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, this.getResources().getDisplayMetrics());
        plateText.setTextSize(15);
        int px80 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, this.getResources().getDisplayMetrics());
        plateText.setHeight(px80);
        int px300 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, this.getResources().getDisplayMetrics());
        plateText.setWidth(px300);

        plateText.setGravity(Gravity.CENTER_VERTICAL);





        TextView plateID = createPlateIDEntry(plate);
        final String id = plateID.getText().toString();
        plateText.setId(Integer.parseInt(plateID.getText().toString()));



        plateText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Renewal for this plate", Toast.LENGTH_SHORT).show();
                //String id = plateID.getText().toString();

                plateText.setBackgroundColor(getResources().getColor(R.color.history_clicked_background));
                Log.e("The id is : ", id);
                RenewFragment renewFragment= new RenewFragment();

                Bundle bundle = new Bundle();
                bundle.putString("ID",id);

                renewFragment.setArguments(bundle);


                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                bottomNavigationView.setSelectedItemId(R.id.action_renew);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_layout, renewFragment, "findThisFragment")
                        .addToBackStack(null)
                        .commit();

            }
        });




   
        ConstraintLayout plateLayout = new ConstraintLayout(getActivity());
        plateLayout.setLayoutParams(linearparams);
        plateLayout.setId(View.generateViewId());






        plateLayout.addView(plateText);







        return plateLayout;
    }


    public LinearLayout createAllPlateEntries(ArrayList<Plate> plates){

        
        LinearLayout innerConstraint = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        innerConstraint.setLayoutParams(lparams);
        innerConstraint.setOrientation(LinearLayout.VERTICAL);
        //innerConstraint.setId(View.generateViewId());



            
            for (Plate plate : plates){
                ConstraintLayout linear = createHistoryEntry(plate);
                innerConstraint.addView(linear);
                //innerSet.connect(linear.getId(), ConstraintSet.LEFT, innerConstraint.getId(), ConstraintSet.LEFT, 0);
                //innerSet.connect(linear.getId(), ConstraintSet.RIGHT, innerConstraint.getId(), ConstraintSet.RIGHT, 0);
            }



        return innerConstraint;
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }


}
