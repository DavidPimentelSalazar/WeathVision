package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.R;

import java.util.List;

public class MetasAdapter extends RecyclerView.Adapter<MetasAdapter.MetaViewHolder> {

    private List<Metas> listaMetas;

    public MetasAdapter(Context context, List<Metas> listaMetas) {
        this.listaMetas = listaMetas;
    }

    @Override
    public MetaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meta_item, parent, false); //
        return new MetaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(MetaViewHolder holder, int position) {
        Metas meta = listaMetas.get(position);
        holder.textFecha.setText(meta.getFechaLimite());
        holder.textTitulo.setText(meta.getTitulo());
        double montoObjetivo = meta.getMontoObjetivo();
        holder.textMonto.setText(String.valueOf(montoObjetivo));

    }

    @Override
    public int getItemCount() {
        return listaMetas.size();
    }

    public static class MetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView textFecha, textTitulo, textMonto;

        public MetaViewHolder(View itemView) {
            super(itemView);
           // imagen = itemView.findViewById(R.id.tu_id_imagen); // Reemplaza con el ID real de tu ImageView
            textFecha = itemView.findViewById(R.id.fecha); // Reemplaza con el ID real de tu TextView de fecha
            textTitulo = itemView.findViewById(R.id.tituloMeta); // Reemplaza con el ID real de tu TextView de título
            textMonto = itemView.findViewById(R.id.montoMeta); // Reemplaza con el ID real de tu TextView de monto
        }
    }
}
