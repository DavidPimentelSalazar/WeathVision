package Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.R;

import java.util.List;
public class CategoriasAdapter extends RecyclerView.Adapter<CategoriasAdapter.CategoryViewHolder> {
    private Context context;
    private List<Categoria> categorias;
    private OnCategoryClickListener listener;
    private int selectedPosition = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(Categoria categoria);
    }

    public CategoriasAdapter(Context context, List<Categoria> categorias, OnCategoryClickListener listener) {
        this.context = context;
        this.categorias = categorias;
        this.listener = listener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.categoria_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);
        holder.textView.setText(categoria.getNombre());
        // Assuming the imagen field contains the drawable resource name (e.g., "salud.png")
        int resId = context.getResources().getIdentifier(categoria.getImagen().replace("@drawable/", "").replace(".png", ""), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.imageView.setImageResource(resId);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Fallback image
        }
        holder.itemView.setBackgroundColor(selectedPosition == position ? 0xFFDDDDDD : 0xFFFFFFFF);
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onCategoryClick(categoria);
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tituloCategoria);
            imageView = itemView.findViewById(R.id.imagenCategoria);
        }
    }
}