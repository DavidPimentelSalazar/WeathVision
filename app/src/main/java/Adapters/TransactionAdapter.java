package Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.R;
import java.util.List;
import androidx.core.content.ContextCompat; // Importa esto

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transacciones;

    public TransactionAdapter(Context context, List<Transaction> transacciones) {
        this.context = context;
        this.transacciones = transacciones;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = transacciones.get(position);
        holder.descripcion.setText(transaction.getCategoria());
        holder.monto.setText(String.format("%.2f€", transaction.getMonto()));
        holder.fecha.setText(transaction.getFecha());

    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView descripcion;
        public TextView monto;
        public TextView fecha;

        public ViewHolder(View itemView) {
            super(itemView);
            descripcion = itemView.findViewById(R.id.text_categoria);
            monto = itemView.findViewById(R.id.text_monto);
            fecha = itemView.findViewById(R.id.text_fecha);
        }
    }
}