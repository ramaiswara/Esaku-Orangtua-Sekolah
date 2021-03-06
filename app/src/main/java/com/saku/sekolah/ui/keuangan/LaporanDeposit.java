package com.saku.sekolah.ui.keuangan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.saku.sekolah.R;
import com.saku.sekolah.adapter.LaporanDepositAdapter;
import com.saku.sekolah.apihelper.BaseApiService;
import com.saku.sekolah.apihelper.UtilsApi;
import com.saku.sekolah.model.keuangan.Deposit;
import com.saku.sekolah.model.keuangan.Deposit2Item;
import com.saku.sekolah.model.keuangan.DepositItem;
import com.saku.sekolah.model.keuangan.ModelLaporanDeposit;
import com.saku.sekolah.preferences.LoadImage;
import com.saku.sekolah.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaporanDeposit extends AppCompatActivity {
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.deposit_nama)
    TextView depositNama;
    @BindView(R.id.deposit_nis)
    TextView depositNis;
    @BindView(R.id.deposit_bank)
    TextView depositBank;
    @BindView(R.id.deposit_saldo)
    TextView depositSaldo;
    @BindView(R.id.deposit_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.tv_deposit_sekolah)
    TextView tvDepositSekolah;
    @BindView(R.id.deposit_kelas)
    TextView depositKelas;
    @BindView(R.id.deposit_angkatan)
    TextView depositAngkatan;
    @BindView(R.id.deposit_jurusan)
    TextView depositJurusan;
    @BindView(R.id.iv_profile)
    ImageView ivProfile;

    private BaseApiService mApiService;
    private Context context;
    private Preferences sp;
    private ArrayList<ModelLaporanDeposit> laporanDepositArrayList = new ArrayList<>();
    private LaporanDepositAdapter adapter;
    LoadImage loadImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_deposit);
        ButterKnife.bind(this);
        context = this;
        sp = new Preferences(context);
        mApiService = UtilsApi.getAPIService(context);
        loadImage=new LoadImage(ivProfile,sp.getLogo());
//        loadImageFromURL(sp.getLogo());
        initLaporanDeposit( sp.getUserLog(), sp.getKodePP(), sp.getLokasi());
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    private void initLaporanDeposit( String nik, String kode_pp, String lokasi) {
        progressBar.setVisibility(View.VISIBLE);
        laporanDepositArrayList.clear();
        mApiService.getDeposit( nik, kode_pp, lokasi)
                .enqueue(new Callback<Deposit>() {
                    @Override
                    public void onResponse(Call<Deposit> call, Response<Deposit> response) {
                        if (response.isSuccessful()) {
                            if (response.body().isStatus()) {
                                List<DepositItem> dataSiswa = response.body().getDaftar();
                                if (dataSiswa.size() > 0) {
                                    for (int i = 0; i < dataSiswa.size(); i++) { //3 = Limit data
                                        tvDepositSekolah.setText(dataSiswa.get(i).getNamaPp());
                                        depositNis.setText(": " + dataSiswa.get(i).getNis());
                                        depositNama.setText(": " + dataSiswa.get(i).getNama());
                                        depositBank.setText(": " + dataSiswa.get(i).getIdBank());
                                        depositSaldo.setText(": Rp. " + response.body().getSoAkhir());
                                        depositKelas.setText(": " + dataSiswa.get(i).getNamaKelas());
                                        depositAngkatan.setText(": " + dataSiswa.get(i).getKodeAkt());
                                        depositJurusan.setText(": " + dataSiswa.get(i).getNamaJur());
                                    }
                                }
                                List<Deposit2Item> dataTransaksi = response.body().getDaftar2();
                                if (dataTransaksi.size() > 0) {
                                    int saldo = 0;
                                    for (int i = 0; i < dataTransaksi.size(); i++) { //3 = Limit data
                                        if (dataTransaksi.get(i).getDc().equals("C")) {
                                            saldo += Integer.parseInt(dataTransaksi.get(i).getDebet()) - Integer.parseInt(dataTransaksi.get(i).getKredit());
                                            laporanDepositArrayList.add(new ModelLaporanDeposit(
                                                    dataTransaksi.get(i).getTgl(),
                                                    dataTransaksi.get(i).getNoBukti(),
                                                    dataTransaksi.get(i).getDc(),
                                                    dataTransaksi.get(i).getKredit(),
                                                    String.valueOf(saldo),
                                                    dataTransaksi.get(i).getKeterangan()
                                            ));
                                        } else {
                                            saldo += Integer.parseInt(dataTransaksi.get(i).getDebet()) - Integer.parseInt(dataTransaksi.get(i).getKredit());
                                            laporanDepositArrayList.add(new ModelLaporanDeposit(
                                                    dataTransaksi.get(i).getTgl(),
                                                    dataTransaksi.get(i).getNoBukti(),
                                                    dataTransaksi.get(i).getDc(),
                                                    dataTransaksi.get(i).getDebet(),
                                                    String.valueOf(saldo),
                                                    dataTransaksi.get(i).getKeterangan()
                                            ));
                                        }
                                    }
                                    adapter = new LaporanDepositAdapter(laporanDepositArrayList);
                                    adapter.notifyDataSetChanged();
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
                                    recyclerView.setLayoutManager(layoutManager);
                                    recyclerView.setAdapter(adapter);
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(context, "Server Bermasalah", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<Deposit> call, Throwable t) {
                        Toast.makeText(context, "Koneksi Bermasalah", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
