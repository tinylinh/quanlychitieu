//
//package com.hynguyen.chitieucanhan.fragment;
//
//import android.content.Intent;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.pdf.PdfDocument;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.FileProvider;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.hynguyen.chitieucanhan.R;
//import com.hynguyen.chitieucanhan.database.AppViewModel;
//import com.hynguyen.chitieucanhan.mdel.ChiTieu;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class ReportFragment extends Fragment {
//    private View view;
//    private AppViewModel appViewModel;
//    private TextView txtNgayBaoCao;
//    private Button btnTaoBaoCao;
//    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//    private DecimalFormat formatter = new DecimalFormat("#,###");
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        view = inflater.inflate(R.layout.fragment_report, container, false);
//
//        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
//        txtNgayBaoCao = view.findViewById(R.id.txtNgayBaoCao);
//        btnTaoBaoCao = view.findViewById(R.id.btnTaoBaoCao);
//
//        txtNgayBaoCao.setText("Ngày: " + sdf.format(new Date()));
//
//        btnTaoBaoCao.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                generatePDFReport();
//            }
//        });
//
//        return view;
//    }
//
//    private void generatePDFReport() {
//        PdfDocument document = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
//        PdfDocument.Page page = document.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//        Paint paint = new Paint();
//
//        int y = 80;
//        paint.setTextSize(16);
//
//        canvas.drawText("BÁO CÁO THU CHI", 250, y, paint);
//        y += 40;
//        canvas.drawText("Ngày: " + sdf.format(new Date()), 50, y, paint);
//        y += 40;
//
//        // Draw table headers
//        canvas.drawText("STT", 50, y, paint);
//        canvas.drawText("Nội dung", 100, y, paint);
//        canvas.drawText("Thu", 300, y, paint);
//        canvas.drawText("Chi", 400, y, paint);
//        canvas.drawText("Số dư", 500, y, paint);
//        y += 20;
//
//        // Get data and draw content
//        LiveData<List<ChiTieu>> chiTieuList = appViewModel.tatCaChiTieu();
//        chiTieuList.observe(getViewLifecycleOwner(), new Observer<List<ChiTieu>>() {
//            @Override
//            public void onChanged(List<ChiTieu> chiTieus) {
//                int stt = 1;
//                int yPos = y;
//                long tongThu = 0;
//                long tongChi = 0;
//
//                for (ChiTieu ct : chiTieus) {
//                    canvas.drawText(String.valueOf(stt), 50, yPos, paint);
//                    canvas.drawText(ct.getNote(), 100, yPos, paint);
//
//                    long amount = Long.parseLong(ct.getMoney().replaceAll(",", ""));
//                    if (ct.getType() == 0) { // Thu
//                        canvas.drawText(formatter.format(amount), 300, yPos, paint);
//                        tongThu += amount;
//                    } else { // Chi
//                        canvas.drawText(formatter.format(amount), 400, yPos, paint);
//                        tongChi += amount;
//                    }
//                    canvas.drawText(formatter.format(tongThu - tongChi), 500, yPos, paint);
//
//                    yPos += 20;
//                    stt++;
//                }
//
//                // Draw totals
//                yPos += 20;
//                canvas.drawText("Tổng cộng:", 100, yPos, paint);
//                canvas.drawText(formatter.format(tongThu), 300, yPos, paint);
//                canvas.drawText(formatter.format(tongChi), 400, yPos, paint);
//                canvas.drawText(formatter.format(tongThu - tongChi), 500, yPos, paint);
//
//                document.finishPage(page);
//
//                // Save and share PDF
//                File pdfFile = new File(getContext().getExternalFilesDir(null), "BaoCaoThuChi.pdf");
//                try {
//                    document.writeTo(new FileOutputStream(pdfFile));
//                    document.close();
//
//                    // Share PDF via email
//                    Uri pdfUri = FileProvider.getUriForFile(getContext(),
//                            getContext().getPackageName() + ".provider", pdfFile);
//
//                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
//                    emailIntent.setType("application/pdf");
//                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Báo cáo thu chi");
//                    emailIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
//                    startActivity(Intent.createChooser(emailIntent, "Gửi báo cáo qua..."));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(getContext(), "Lỗi tạo báo cáo: " + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//}
