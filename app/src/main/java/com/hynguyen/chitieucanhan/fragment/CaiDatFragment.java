package com.hynguyen.chitieucanhan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hynguyen.chitieucanhan.R;
import com.hynguyen.chitieucanhan.activity.DanhMucActivity;
import com.hynguyen.chitieucanhan.activity.ReportActivity;
import com.hynguyen.chitieucanhan.pprofile3;
import com.hynguyen.chitieucanhan.activity.ViTienActivity;
import com.hynguyen.chitieucanhan.database.AppViewModel;
import com.hynguyen.chitieucanhan.mdel.LoaiTienTe;
import com.hynguyen.chitieucanhan.home;
import org.jetbrains.annotations.NotNull;
import com.google.firebase.auth.FirebaseAuth;
import com.hynguyen.chitieucanhan.profilenew;

public class CaiDatFragment extends Fragment implements View.OnClickListener {
    private View view;
    private AppViewModel appViewModel;
    //Danh mục
    private TextView txtDanhMuc;
    //Ví tiền
    private TextView txtViTien;
    //Loại tiền tệ
    private LiveData<LoaiTienTe> tienTeLiveData;
    private RelativeLayout rlLoaiTienTe;
    private TextView txtLoaiTienTe;
    private TextView txtDangxuat;
    private TextView txtReport;
    private TextView txtProfile;
    private Fragment fm;
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        //Khai báo View
        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        //Danh mục
        txtDanhMuc = view.findViewById(R.id.txtDanhMuc);
        txtDanhMuc.setOnClickListener(this);
        //Ví tiền
        txtViTien = view.findViewById(R.id.txtViTien);
        txtViTien.setOnClickListener(this);
        //Loại tiền tệ
        txtDangxuat = view.findViewById(R.id.txtDangxuat);
        txtDangxuat.setOnClickListener(this);
        //
        txtProfile = view.findViewById(R.id.txtProfile);
        txtProfile.setOnClickListener(this);

        //
        txtReport = view.findViewById(R.id.txtReport);
        txtReport.setOnClickListener(this);
        //
        rlLoaiTienTe = view.findViewById(R.id.rlLoaiTienTe);
        rlLoaiTienTe.setOnClickListener(this);
        rlLoaiTienTe.setOnClickListener(this);
        txtLoaiTienTe = view.findViewById(R.id.txtLoaiTienTe);
        tienTeLiveData = appViewModel.xuatLoaiTienTe();
        tienTeLiveData.observe(getActivity(), new Observer<LoaiTienTe>() {
            @Override
            public void onChanged(LoaiTienTe loaiTienTe) {
                txtLoaiTienTe.setText(loaiTienTe.getName());
            }
        }

        );

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtDanhMuc:
                startActivity(new Intent(getActivity(), DanhMucActivity.class));
                break;
            case R.id.txtViTien:
                startActivity(new Intent(getActivity(), ViTienActivity.class));
                break;
            case R.id.rlLoaiTienTe:
                showBottomSheetLoaiTienTe();
                break;
            case  R.id.txtDangxuat:FirebaseAuth.getInstance().signOut();startActivity(new Intent(getActivity(), home.class));
                break;
            case R.id.txtProfile:
                startActivity(new Intent(getActivity(), profilenew.class));
                break;
            case R.id.txtReport:
                Intent intent = new Intent(getActivity(), ReportActivity.class);
                startActivity(intent);


        }
    }

    public void showBottomSheetLoaiTienTe() {
        SuaLoaiTienTeFragment suaLoaiTienTeFragment = SuaLoaiTienTeFragment.newInstance();
        suaLoaiTienTeFragment.show(getActivity().getSupportFragmentManager(), SuaLoaiTienTeFragment.TAG);
    }

}