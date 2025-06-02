package Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Map<String, String> categoryImageMap;
    private OnTransactionDeleteListener deleteListener;
    private int expandedPosition = -1;

    public interface OnTransactionDeleteListener {
        void onTransactionDelete(Transaction transaction);
    }


    public TransactionAdapter(Context context, List<Transaction> transacciones,List<Categoria> categorias) {
        this.context = context;
        this.transacciones = transacciones;
        this.categoryImageMap = new HashMap<>();


    }
    public TransactionAdapter(Context context, List<Transaction> transacciones, List<Categoria> categorias, OnTransactionDeleteListener deleteListener) {
        this.context = context;
        this.transacciones = transacciones;
        this.categoryImageMap = new HashMap<>();
        this.deleteListener = deleteListener;
        updateCategorias(categorias);
    }

    public void updateCategorias(List<Categoria> categorias) {
        categoryImageMap.clear();
        for (Categoria categoria : categorias) {
            String categoryName = categoria.getNombre() != null ? categoria.getNombre().trim().toLowerCase() : "";
            String imagePath = categoria.getImagen() != null ? categoria.getImagen() : "";
            categoryImageMap.put(categoryName, imagePath);
        }
        notifyDataSetChanged();
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

        // Bind primary fields
        holder.descripcion.setText(transaction.getCategoria() != null ? transaction.getCategoria() : "Sin categoría");
        holder.monto.setText(String.format("%.2f€", transaction.getMonto()));
        holder.tipoChica.setText(transaction.getTipo());
        holder.fechaChica.setText(transaction.getFecha());

        // Bind details fields
        holder.tipo.setText("Tipo: " + (transaction.getTipo() != null ? transaction.getTipo() : "N/A"));
        holder.descripcionDetails.setText("Descripción: " + (transaction.getDescripcion() != null ? transaction.getDescripcion() : "N/A"));
        holder.fecha.setText("Fecha: " + (transaction.getFecha() != null ? transaction.getFecha() : "N/A"));

        // Set text color based on transaction type
        if (transaction.getTipo() != null && transaction.getTipo().equalsIgnoreCase("Ingreso")) {
            holder.monto.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else if (transaction.getTipo() != null && transaction.getTipo().equalsIgnoreCase("Gasto")) {
            holder.monto.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.monto.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        // Load the image based on the category
        String categoryName = transaction.getCategoria() != null ? transaction.getCategoria().trim().toLowerCase() : "";
        if (categoryName.isEmpty()) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            String imagePath = categoryImageMap.get(categoryName);
            if (imagePath != null && !imagePath.isEmpty()) {
                String resourceName = imagePath.replace("@drawable/", "")
                        .replace(".png", "")
                        .replace(".jpg", "")
                        .trim();
                int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.icon.setImageResource(resId);
                } else {
                    holder.icon.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.icon.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Toggle details visibility
        boolean isExpanded = position == expandedPosition;
        holder.detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.fechaChica.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.tipoChica.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

        // Handle item click to toggle details
        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position;
            notifyDataSetChanged();
        });

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTransactionDelete(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView descripcion;
        public TextView monto;
        public TextView tipo;
        public TextView descripcionDetails, fechaChica, tipoChica;

        public TextView fecha;
        public ImageView icon;
        public LinearLayout detailsContainer;
        public Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            descripcion = itemView.findViewById(R.id.text_categoria);
            monto = itemView.findViewById(R.id.text_monto);
            tipo = itemView.findViewById(R.id.text_tipo);
            descripcionDetails = itemView.findViewById(R.id.text_descripcion);
            fecha = itemView.findViewById(R.id.text_fecha);
            icon = itemView.findViewById(R.id.icon);
            detailsContainer = itemView.findViewById(R.id.details_container);
            deleteButton = itemView.findViewById(R.id.btn_delete);

            fechaChica = itemView.findViewById(R.id.fechaChica);
            tipoChica = itemView.findViewById(R.id.tipoChico);
        }
    }
}