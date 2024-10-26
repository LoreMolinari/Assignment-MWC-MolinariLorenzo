package com.example.stepappv4.ui.Report;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorLong;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.databinding.FragmentGalleryBinding;
import com.example.stepappv4.R;

public class ReportFragment extends Fragment {
    private AnyChartView anyChartView;
    private Button dailyHourlyButton;
    private FragmentGalleryBinding binding;
    private String current_time;
    private boolean daily = true;
    private ViewGroup chartContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Date cDate = new Date();
        current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

        chartContainer = root.findViewById(R.id.chart_container);

        dailyHourlyButton = root.findViewById(R.id.dailyHourlyButton);
        dailyHourlyButton.setText(R.string.hourly_report);
        setupChartView();

        dailyHourlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daily = !daily;

                if (daily) {
                    dailyHourlyButton.setText(R.string.hourly_report);
                    Toast.makeText(getContext(), "Showing Daily Report", Toast.LENGTH_SHORT).show();
                } else {
                    dailyHourlyButton.setText(R.string.daily_report);
                    Toast.makeText(getContext(), "Showing Hourly Report", Toast.LENGTH_SHORT).show();
                }

                setupChartView();
            }
        });

        return root;
    }

    private void setupChartView() {
        if (anyChartView != null) {
            chartContainer.removeView(anyChartView);
        }

        anyChartView = new AnyChartView(getContext());
        anyChartView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        anyChartView.setProgressBar(binding.getRoot().findViewById(R.id.loadingBar));
        anyChartView.setBackgroundColor("#00000000");

        chartContainer.addView(anyChartView);

        if (daily) {
            Cartesian cartesian = createDailyColumnChart();
            anyChartView.setChart(cartesian);
        } else {
            Cartesian cartesian = createColumnChart();
            anyChartView.setChart(cartesian);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Cartesian createColumnChart(){
        //***** Read data from SQLiteDatabase *********/
        // TODO 1 (YOUR TURN): Get the map with hours and number of steps for today
        //  from the database and assign it to variable stepsByHour
        Map<Integer, Integer> stepsByHour = StepAppOpenHelper.loadStepsByHour(getContext(), current_time);

        // TODO 2 (YOUR TURN): Creating a new map that contains hours of the day from 0 to 23 and
        //  number of steps during each hour set to 0
        Map<Integer, Integer> graph_map = new TreeMap<>();
        for(int i =0; i<23; i++){
            graph_map.put(i, 0);
        }

        // TODO 3 (YOUR TURN): Replace the number of steps for each hour in graph_map
        //  with the number of steps read from the database
        graph_map.putAll(stepsByHour);

        //***** Create column chart using AnyChart library *********/
        // TODO 4: Create and get the cartesian coordinate system for column chart
        Cartesian cartesian = AnyChart.column();

        // TODO 5: Create data entries for x and y axis of the graph
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<Integer,Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        // TODO 6: Add the data to column chart and get the columns
        Column column = cartesian.column(data);

        // TODO 7 (YOUR TURN): Change the color of column chart and its border
        column.fill("#1EB980");
        column.stroke("#1EB980");


        // TODO 8: Modifying properties of tooltip
        column.tooltip()
                .titleFormat("At hour: {%X}")
                .format("{%Value} Steps")
                .anchor(Anchor.RIGHT_BOTTOM);

        // TODO 9 (YOUR TURN): Modify column chart tooltip properties
        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        // Modifying properties of cartesian
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);


        // TODO 10 (YOUR TURN): Modify the UI of the cartesian
        cartesian.yAxis(0).title("Number of steps");
        cartesian.xAxis(0).title("Hour");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }

    public Cartesian createDailyColumnChart() {
        Map<String, Integer> stepsByDay = StepAppOpenHelper.loadStepsByDay(getContext());

        List<DataEntry> data = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : stepsByDay.entrySet()) {
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        Cartesian cartesian = AnyChart.column();
        Column column = cartesian.column(data);

        column.fill("#1EB980");
        column.stroke("#1EB980");

        column.tooltip()
                .titleFormat("Date: {%X}")
                .format("{%Value} Steps")
                .anchor(Anchor.RIGHT_BOTTOM)
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);
        cartesian.yAxis(0).title("Number of steps");
        cartesian.xAxis(0).title("Day");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }
}