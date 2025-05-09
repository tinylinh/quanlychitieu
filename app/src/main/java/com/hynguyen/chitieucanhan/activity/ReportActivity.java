package com.hynguyen.chitieucanhan.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.hynguyen.chitieucanhan.R;
import com.hynguyen.chitieucanhan.database.AppViewModel;
import com.hynguyen.chitieucanhan.mdel.ChiTieu;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = "ReportActivity";
    private AppViewModel appViewModel;
    private TextView txtDate;
    private Button btnExportPDF;
    private List<ChiTieu> listChiTieu;
    private NumberFormat numberFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        txtDate = findViewById(R.id.txtDate);
        btnExportPDF = findViewById(R.id.btnExportPDF);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtDate.setText("Ngày: " + sdf.format(new Date()));

        loadData();

        btnExportPDF.setOnClickListener(v -> createAndSharePDF());
    }

    private void loadData() {
        // Load data from database and observe LiveData
        appViewModel.tatCaChiTieu().observe(this, chiTieus -> {
            listChiTieu = chiTieus;
            Log.d(TAG, "Loaded " + (listChiTieu != null ? listChiTieu.size() : 0) + " transactions");
            updateUI();
        });
    }

    private void updateUI() {
        if (listChiTieu == null || listChiTieu.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
            return;
        }

        long tongThu = 0;
        long tongChi = 0;

        for (ChiTieu chiTieu : listChiTieu) {
            try {
                long amount = Long.parseLong(chiTieu.getMoney());
                if (chiTieu.getType() == 1) { // Thu
                    tongThu += amount;
                } else if (chiTieu.getType() == 2) { // Chi
                    tongChi += amount;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid money format for transaction: " + chiTieu.getMoney(), e);
            }
        }

        // Update TextViews (if present in activity_report.xml)
        TextView txtTongThu = findViewById(R.id.txtTongThu);
        TextView txtTongChi = findViewById(R.id.txtTongChi);
        if (txtTongThu != null) {
            txtTongThu.setText("Tổng thu: " + numberFormat.format(tongThu) + " VND");
        }
        if (txtTongChi != null) {
            txtTongChi.setText("Tổng chi: " + numberFormat.format(tongChi) + " VND");
        }
    }

    private void createAndSharePDF() {
        if (listChiTieu == null || listChiTieu.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu để xuất PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFolder = new File(getExternalFilesDir(null), "pdfs");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }

            File pdfFile = new File(pdfFolder, "report_" + System.currentTimeMillis() + ".pdf");
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("TỔNG HỢP TÌNH HÌNH THU CHI", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Add date
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
            Paragraph date = new Paragraph("Ngày: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()), normalFont);
            document.add(date);
            document.add(new Paragraph("\n"));

            // Create table
            PdfPTable table = new PdfPTable(5); // 5 columns: STT, Date, Amount, Type, Note
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 2, 1, 3});

            // Add headers
            addTableHeader(table);

            // Add data
            addTableData(table);

            document.add(table);
            document.close();

            // Share PDF
            sharePDF(pdfFile);

        } catch (Exception e) {
            Log.e(TAG, "Error creating PDF", e);
            Toast.makeText(this, "Lỗi khi tạo PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addTableHeader(PdfPTable table) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        String[] headers = {"STT", "Ngày", "Số tiền", "Loại", "Ghi chú"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableData(PdfPTable table) {
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < listChiTieu.size(); i++) {
            ChiTieu chiTieu = listChiTieu.get(i);

            // STT
            PdfPCell cellSTT = new PdfPCell(new Phrase(String.valueOf(i + 1), dataFont));
            cellSTT.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellSTT);

            // Ngày
            PdfPCell cellDate = new PdfPCell(new Phrase(chiTieu.getDate() != null ? sdf.format(chiTieu.getDate()) : "", dataFont));
            cellDate.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellDate);

            // Số tiền
            String amountStr = chiTieu.getMoney() != null ? chiTieu.getMoney() : "0";
            try {
                long amount = Long.parseLong(amountStr);
                PdfPCell cellAmount = new PdfPCell(new Phrase(numberFormat.format(amount) + " VND", dataFont));
                cellAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellAmount);
            } catch (NumberFormatException e) {
                PdfPCell cellAmount = new PdfPCell(new Phrase("N/A", dataFont));
                cellAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellAmount);
                Log.e(TAG, "Invalid money format: " + amountStr, e);
            }

            // Loại
            String type = chiTieu.getType() == 1 ? "Thu" : "Chi";
            PdfPCell cellType = new PdfPCell(new Phrase(type, dataFont));
            cellType.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellType);

            // Ghi chú
            PdfPCell cellNote = new PdfPCell(new Phrase(chiTieu.getNote() != null ? chiTieu.getNote() : "", dataFont));
            cellNote.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cellNote);
        }
    }

    private void sharePDF(File pdfFile) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Chia sẻ báo cáo"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing PDF", e);
            Toast.makeText(this, "Lỗi khi chia sẻ PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}