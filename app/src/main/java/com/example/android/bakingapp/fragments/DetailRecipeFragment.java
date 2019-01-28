package com.example.android.bakingapp.fragments;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapters.DetailAdapter;
//import com.example.android.bakingapp.data.WidgetRemoteViewsService;
import com.example.android.bakingapp.data.BakingAppWidget;
import com.example.android.bakingapp.data.WidgetRemoteViewsService;
import com.example.android.bakingapp.models.Ingredient;
import com.example.android.bakingapp.models.Recipe;
import com.example.android.bakingapp.models.Step;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailRecipeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = DetailRecipeFragment.class.getSimpleName();

    public static Recipe recipe;

    public static List<Step> stepList;

    public static final String recipeKey = "recipe";

    List<Ingredient> ingredientList;

    DetailAdapter detailAdapter;

    OnRecipeStepClickListener onRecipeStepClickListener;

    @BindView(R.id.detail_recycler_view)
    RecyclerView detailRecyclerView;

    @BindView(R.id.ingredients_text_view)
    TextView ingredientsTextView;

    SharedPreferences sharedPreferences;

    public static final String preferenceId = "preferenceId";

    public static final String preferenceName = "preferenceName";

    public static final String preferenceIngredients = "preferenceIngredients";

    public static final String preferences = "preferences";

    public static String recipeName;

    public static String ingredientsString;

    public static final String recipeNameKey = "recipeName";


    WidgetRemoteViewsService widgetRemoteViewsService;

    public static final String stepListKey = "step";


    public static final String ingredientListKey = "ingredient";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


    public interface OnRecipeStepClickListener {
        void onRecipeStepSelected(int position);
    }

    public DetailRecipeFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            recipeName = savedInstanceState.getString(recipeNameKey);
            recipe = savedInstanceState.getParcelable(recipeKey);
            stepList = savedInstanceState.getParcelableArrayList(stepListKey);
            ingredientList = savedInstanceState.getParcelableArrayList(ingredientListKey);
        } else {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getParcelableExtra(recipeKey) != null) {
                recipe = getActivity().getIntent().getParcelableExtra(recipeKey);
                recipeName = recipe.getName();
                stepList = getActivity().getIntent().getParcelableArrayListExtra(stepListKey);
                ingredientList = getActivity().getIntent().getParcelableArrayListExtra(ingredientListKey);
            }
        }

        recipeName = recipe.getName();

        if (getActivity() != null) {
            getActivity().setTitle(recipeName);
        }

        stepList = recipe.getStepList();

        ingredientList = recipe.getIngredientList();

        int recipeId = recipe.getId();

        View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        Context context = rootView.getContext();

        detailAdapter = new DetailAdapter(context, stepList, onRecipeStepClickListener);

        final FragmentActivity fragmentActivity = getActivity();
        ButterKnife.bind(this, rootView);

        detailRecyclerView = rootView.findViewById(R.id.detail_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(fragmentActivity);
        detailRecyclerView.setLayoutManager(layoutManager);
        detailRecyclerView.setAdapter(detailAdapter);

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < ingredientList.size(); i++) {
            int quantityInt = ingredientList.get(i).getIngredientQuantity();
            String quantity = String.valueOf(quantityInt);
            String measure = ingredientList.get(i).getIngredientMeasure();
            String ingredient = ingredientList.get(i).getIngredientName();
            String ingredientLine = quantity + " " + measure + " " + ingredient + "\n";
            stringBuilder.append(ingredientLine);
        }

        ingredientsString = stringBuilder.toString();

        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(preferenceId, recipeId);
        editor.putString(preferenceName, recipeName);
        editor.putString(preferenceIngredients, ingredientsString);
        editor.apply();


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        Intent widgetIntent = new Intent(context, BakingAppWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), BakingAppWidget.class.getName()));

        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.baking_app_widget);
//        appWidgetManager.updateAppWidget(new ComponentName(context, BakingAppWidget.class), remoteViews);

        detailRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecipeStepClickListener.onRecipeStepSelected(detailRecyclerView.getId());
            }
        });

        populateDetailRecipeView(ingredientsString, stepList);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(recipeNameKey, recipeName);
        outState.putParcelable(recipeKey, recipe);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onRecipeStepClickListener = (OnRecipeStepClickListener) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.toString() + "must implement OnDetailRecipeStepClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onRecipeStepClickListener = null;
    }

    public void populateDetailRecipeView(String ingredients, List<Step> stepList) {
        this.stepList = stepList;
        detailAdapter.setSteps(stepList);
        ingredientsTextView.setText(ingredients);
    }


}
