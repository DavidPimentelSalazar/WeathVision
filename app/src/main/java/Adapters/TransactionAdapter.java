package Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private static final String TAG = "TransactionAdapter";
    private Context context;
    private List<Transaction> transacciones;
    private Map<String, String> categoryImageMap; // Map to store category name to image path

    public TransactionAdapter(Context context, List<Transaction> transacciones, List<Categoria> categorias) {
        this.context = context;
        this.transacciones = transacciones;
        this.categoryImageMap = new HashMap<>();
        // Populate the map with category names and their image paths
        for (Categoria categoria : categorias) {
            categoryImageMap.put(categoria.getNombre(), categoria.getImagen());
        }
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
        Log.d(TAG, "Binding transaction: tipo=" + transaction.getTipo() + ", monto=" + transaction.getMonto() + ", categoria=" + transaction.getCategoria());

        holder.descripcion.setText(transaction.getCategoria() != null ? transaction.getCategoria() : "Sin categoría");
        holder.monto.setText(String.format("%.2f€", transaction.getMonto()));
        holder.fecha.setText(transaction.getFecha() != null ? transaction.getFecha() : "");

        // Set text color based on transaction type
        if (transaction.getTipo() != null && transaction.getTipo().equalsIgnoreCase("Ingreso")) {
            holder.monto.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else if (transaction.getTipo() != null && transaction.getTipo().equalsIgnoreCase("Gasto")) {
            holder.monto.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            Log.w(TAG, "Unknown transaction tipo: " + transaction.getTipo());
            holder.monto.setTextColor(ContextCompat.getColor(context, android.R.color.black)); // Default color
        }

        // Load the image based on the category
        String imagePath = categoryImageMap.get(transaction.getCategoria());
        if (imagePath != null) {
            int resId = context.getResources().getIdentifier(
                    imagePath.replace("@drawable/", "").replace(".png", ""),
                    "drawable",
                    context.getPackageName());
            if (resId != 0) {
                holder.icon.setImageResource(resId);
            } else {
                holder.icon.setImageResource(android.R.drawable.ic_menu_gallery); // Fallback image
            }
        } else {
            holder.icon.setImageResource(android.R.drawable.ic_menu_gallery); // Fallback image
        }
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView descripcion;
        public TextView monto;
        public TextView fecha;
        public ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            descripcion = itemView.findViewById(R.id.text_categoria);
            monto = itemView.findViewById(R.id.text_monto);
            fecha = itemView.findViewById(R.id.text_fecha);
            icon = itemView.findViewById(R.id.icon); // Reference to the ImageView
        }
    }
}