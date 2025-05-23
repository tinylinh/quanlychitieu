package com.hynguyen.chitieucanhan.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.hynguyen.chitieucanhan.R;
import com.hynguyen.chitieucanhan.activity.ViTienActivity;
import com.hynguyen.chitieucanhan.adapter.ChiTieuAdapter;
import com.hynguyen.chitieucanhan.adapter.ChonThangAdapter;
import com.hynguyen.chitieucanhan.adapter.ViTienAdapter;
import com.hynguyen.chitieucanhan.database.AppViewModel;
import com.hynguyen.chitieucanhan.mdel.ChiTieu;
import com.hynguyen.chitieucanhan.mdel.ViTien;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.util.Comparator;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private View view;

    //Database
    private AppViewModel appViewModel;
    //Chọn ngày
    private ExpandableLayout expMonth;
    private GridView gvPickMonth;
    private TextView txtYear;
    private ChonThangAdapter chonThangAdapter;
    private ImageButton btnLastYear, btnNextYear;
    private TextView txtTongThu, txtTongChi;
    //Chart
    private PieChart lcThongKeThuChi;
    //Dữ liệu
    private LiveData<List<ChiTieu>> chiTieuLiveData;
    private List<ChiTieu> listChiTieu;
    private long day;
    private long month;
    private long year;

    //Ví tiền
    private RecyclerView rvViTien;
    private ViTienAdapter viTienAdapter;
    private LiveData<List<ViTien>> viTienLiveData;

    //Thu Chi
    private RecyclerView rvCacChiTieu;
    private ChiTieuAdapter chiTieuAdapter;
    private ImageButton btnSort;
    private boolean isAscending = true;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        lcThongKeThuChi = view.findViewById(R.id.lcThongKeThuChi);
        txtTongThu = view.findViewById(R.id.txtTongThu);
        txtTongChi = view.findViewById(R.id.txtTongChi);
        buttonScrollToTop();
        addDatabase();
        chonThangNam();
        addView();
        lineChart();
        return view;
    }

    private void addDatabase() {
        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
    }

    private void showThongTinBieuDo(Entry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        Date date = new Date((long) entry.getX());
        builder.setMessage("Ngày: " + date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getYear()
                + "\nSố tiền: " + numberFormat(String.valueOf((int)entry.getY())));
        builder.setCancelable(true);
        // Create AlertDialog:
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void addView() {

        //Line chart
        chiTieuLiveData = appViewModel.tatCaChiTieu();
        listChiTieu = appViewModel.xuatChiTieu();
        chiTieuLiveData.observe(getViewLifecycleOwner(), new Observer<List<ChiTieu>>() {
            @Override
            public void onChanged(List<ChiTieu> chiTieuList) {
                chiTieuAdapter.submitList(chiTieuList);
                listChiTieu = new ArrayList<>(chiTieuList); // Tạo bản sao
                lineChart();
            }
        });
        lcThongKeThuChi.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showThongTinBieuDo(e);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        //Ví Tiền
        rvViTien = view.findViewById(R.id.rvViTien);
        rvViTien.setLayoutManager(new LinearLayoutManager(getContext()));
        viTienAdapter = new ViTienAdapter(getContext());
        rvViTien.setAdapter(viTienAdapter);
        viTienLiveData = appViewModel.tatCaViTien();
        viTienLiveData.observe(getViewLifecycleOwner(), new Observer<List<ViTien>>() {
            @Override
            public void onChanged(List<ViTien> viTienList) {
                viTienAdapter.submitList(viTienList);
            }
        });
        viTienAdapter.setOnItemClickListener(new ViTienAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViTien viTien) {
                startActivity(new Intent(getActivity(), ViTienActivity.class));
            }
        });

        // Chi Tiêu
        rvCacChiTieu = view.findViewById(R.id.rvCacChiTieu);
        rvCacChiTieu.setLayoutManager(new LinearLayoutManager(getContext()));
        chiTieuAdapter = new ChiTieuAdapter(getContext());
        rvCacChiTieu.setAdapter(chiTieuAdapter);

        // Khởi tạo nút Sort
        btnSort = view.findViewById(R.id.btnSort);
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTransactions();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void lineChart() {
        List<Entry> listChi = new ArrayList<>();
        List<Entry> listThu = new ArrayList<>();
        //Tìm khoản chi
        for (int i = 0; i < listChiTieu.size(); i++) {
            ChiTieu chitieui = listChiTieu.get(i);
            long k = chitieui.getDate().getTime();
            if (chitieui.getDate().getYear() == year) {
                if (chitieui.getType() == 2) {
                    long tongChi = 0;
                    tongChi += Long.parseLong(chitieui.getMoney());
                    for (int j = i + 1; j < listChiTieu.size(); j++) {
                        ChiTieu chitieuj = listChiTieu.get(j);
                        if (chitieui.getDate().equals(chitieuj.getDate()) && chitieui.getType() == chitieuj.getType()) {
                            tongChi += Long.parseLong(chitieuj.getMoney());
                            i = j;
                        }
                    }
                    listChi.add(new Entry(k, tongChi));
                }
            }
        }
        //Tìm khoản thu
        for (int i = 0; i < listChiTieu.size(); i++) {
            ChiTieu chitieui = listChiTieu.get(i);
            long h = chitieui.getDate().getTime();
            if (chitieui.getDate().getYear() == year) {
                if (chitieui.getType() == 1) {
                    long tongThu = 0;
                    tongThu += Long.parseLong(chitieui.getMoney());
                    for (int j = i + 1; j < listChiTieu.size(); j++) {
                        ChiTieu chitieuj = listChiTieu.get(j);
                        if (chitieui.getDate().equals(chitieuj.getDate()) && chitieui.getType() == chitieuj.getType()) {
                            tongThu += Long.parseLong(chitieuj.getMoney());
                            i = j;
                        }
                    }
                    listThu.add(new Entry(h, tongThu));
                }
            }
        }
//        LineDataSet chitieu = new LineDataSet(listChi, "Chi tiêu");
//        chitieu.setColor(getResources().getColor(R.color.button_cancel));
//        LineDataSet thunhap = new LineDataSet(listThu, "Thu nhập");
        //pie chart
        long tongChi = 0;
        long tongThu = 0;
        for (int i = 0; i < listChi.size(); i++) {
            tongChi += listChi.get(i).getY();
        }
        for (int i = 0; i < listThu.size(); i++) {
            tongThu += listThu.get(i).getY();
        }
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#E74C3C"));
        colors.add(Color.parseColor("#5DADE2"));
        List<PieEntry> listPie = new ArrayList<>();
        if (tongThu > 0) {
            listPie.add(new PieEntry(tongThu, "Thu"));
        }

        // Check if chi is greater than 0 and add it to the entries
        if (tongChi > 0) {
            listPie.add(new PieEntry(tongChi, "Chi"));
        }
        PieDataSet thuchi = new PieDataSet(listPie, "");
        thuchi.setColors(colors);
        lcThongKeThuChi.animateX(200);
        lcThongKeThuChi.getDescription().setEnabled(false);
//        LineData lineData = new LineData();
//        lineData.addDataSet(thunhap);
//        lineData.addDataSet(chitieu);

        PieData pieData = new PieData(thuchi);
        pieData.setDrawValues(true);
        Legend legend = lcThongKeThuChi.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
        lcThongKeThuChi.setData(pieData);
        lcThongKeThuChi.invalidate();
        txtTongThu.setText(numberFormat(String.valueOf(tongThu))+ " VND");
        txtTongThu.setBackgroundColor(colors.get(0));
        txtTongChi.setText(numberFormat(String.valueOf(tongChi))+ " VND");
        txtTongChi.setBackgroundColor(colors.get(1));
    }

    @SuppressLint("SetTextI18n")
    private void lineChartMonth(long month) {
        List<Entry> listChi = new ArrayList<>();
        List<Entry> listThu = new ArrayList<>();
        //Tìm khoản chi
        for (int i = 0; i < listChiTieu.size(); i++) {
            ChiTieu chitieui = listChiTieu.get(i);
            if (chitieui.getDate().getYear() == year && chitieui.getDate().getMonth() == month) {
                int k = chitieui.getDate().getDate();
                if (chitieui.getType() == 2) {
                    long tongChi = 0;
                    tongChi += Long.parseLong(chitieui.getMoney());
                    for (int j = i + 1; j < listChiTieu.size(); j++) {
                        ChiTieu chitieuj = listChiTieu.get(j);
                        if (chitieui.getDate().equals(chitieuj.getDate()) && chitieui.getType() == chitieuj.getType()) {
                            tongChi += Long.parseLong(chitieuj.getMoney());
                            i = j;
                        }
                    }
                    listChi.add(new Entry(k, tongChi));
                }
            }
        }
        //Tìm khoản thu
        for (int i = 0; i < listChiTieu.size(); i++) {
            ChiTieu chitieui = listChiTieu.get(i);
            if (chitieui.getDate().getYear() == year && chitieui.getDate().getMonth() == month) {
                int k = chitieui.getDate().getDate();
                if (chitieui.getType() == 1) {
                    long tongThu = 0;
                    tongThu += Long.parseLong(chitieui.getMoney());
                    for (int j = i + 1; j < listChiTieu.size(); j++) {
                        ChiTieu chitieuj = listChiTieu.get(j);
                        if (chitieui.getDate().equals(chitieuj.getDate()) && chitieui.getType() == chitieuj.getType()) {
                            tongThu += Long.parseLong(chitieuj.getMoney());
                            i = j;
                        }
                    }
                    listThu.add(new Entry(k, tongThu));
                }
            }
        }
        long tongChi = 0;
        long tongThu = 0;
        for (int i = 0; i < listChi.size(); i++) {
            tongChi += listChi.get(i).getY();
        }
        for (int i = 0; i < listThu.size(); i++) {
            tongThu += listThu.get(i).getY();
        }
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.BLUE);
        List<PieEntry> listPie = new ArrayList<>();
        if (tongThu > 0) {
            listPie.add(new PieEntry(tongThu, "Thu"));
        }

        // Check if chi is greater than 0 and add it to the entries
        if (tongChi > 0) {
            listPie.add(new PieEntry(tongChi, "Chi"));
        }
        PieDataSet thuchi = new PieDataSet(listPie, "");
        thuchi.setColors(colors);
        lcThongKeThuChi.animateX(200);
        lcThongKeThuChi.getDescription().setEnabled(false);


        PieData pieData = new PieData(thuchi);
        pieData.setDrawValues(true);
        Legend legend = lcThongKeThuChi.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
        lcThongKeThuChi.setData(pieData);
        lcThongKeThuChi.invalidate();
        txtTongThu.setText(numberFormat(String.valueOf(tongThu))+ " VND");
        txtTongThu.setBackgroundColor(colors.get(0));
        txtTongChi.setText(numberFormat(String.valueOf(tongChi))+ " VND");
        txtTongChi.setBackgroundColor(colors.get(1));
    }

    private void chonThangNam() {
        expMonth = view.findViewById(R.id.expMonth);
        gvPickMonth = view.findViewById(R.id.gvPickMonth);
        txtYear = view.findViewById(R.id.txtYear);
        Calendar calendar = Calendar.getInstance();
        txtYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        txtYear.setOnClickListener(this);
        chonThangAdapter = new ChonThangAdapter(getContext());
        gvPickMonth.setAdapter(chonThangAdapter);
        btnLastYear = view.findViewById(R.id.btnLastYear);
        btnNextYear = view.findViewById(R.id.btnNextYear);
        btnLastYear.setOnClickListener(this);
        btnNextYear.setOnClickListener(this);
        gvPickMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id < 12) {
                    lineChartMonth(id);
                } else {
                    lineChart();
                }
            }
        });
        getYear();
    }

    private void getYear() {
        year = Long.parseLong(txtYear.getText().toString());
    }

    //Nút cuộn lên đầu trang
    private void buttonScrollToTop() {
        ImageButton btnScrollToTop = view.findViewById(R.id.btnScrollToTop);
        ScrollView scrHome = view.findViewById(R.id.scrHome);
        btnScrollToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrHome.post(new Runnable() {
                    @Override
                    public void run() {
                        scrHome.smoothScrollTo(0, 0);
                    }
                });
            }
        });
        scrHome.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrHome.getScrollY();
                if (scrollY > 500) {
                    btnScrollToTop.setVisibility(View.VISIBLE);
                } else {
                    btnScrollToTop.setVisibility(View.GONE);
                }
            }
        });
    }
    private void sortTransactions() {
        if (listChiTieu == null || listChiTieu.isEmpty()) {
            Toast.makeText(getContext(), "Không có giao dịch để sắp xếp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo bản sao của danh sách để sắp xếp
        List<ChiTieu> sortedList = new ArrayList<>(listChiTieu);

        // Sắp xếp theo số tiền
        Collections.sort(sortedList, new Comparator<ChiTieu>() {
            @Override
            public int compare(ChiTieu o1, ChiTieu o2) {
                long amount1 = Long.parseLong(o1.getMoney());
                long amount2 = Long.parseLong(o2.getMoney());
                return isAscending ? Long.compare(amount1, amount2) : Long.compare(amount2, amount1);
            }
        });

        // Cập nhật danh sách cho adapter
        chiTieuAdapter.submitList(sortedList);

        // Đổi trạng thái sắp xếp và xoay biểu tượng nút
        isAscending = !isAscending;
        if (btnSort != null) {
            btnSort.setRotation(isAscending ? 0 : 180);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtYear:
                expMonth.toggle();
                break;
            case R.id.btnLastYear:
                txtYear.setText(Integer.parseInt(txtYear.getText().toString()) - 1 + "");
                getYear();
                lineChart();
                break;
            case R.id.btnNextYear:
                txtYear.setText(Integer.parseInt(txtYear.getText().toString()) + 1 + "");
                getYear();
                lineChart();
                break;

        }
    }

    //Định dạng số tiền
    public String numberFormat(String string) {
        Long number = Long.parseLong(string);
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        String formattedString = formatter.format(number);
        return formattedString;
    }
}