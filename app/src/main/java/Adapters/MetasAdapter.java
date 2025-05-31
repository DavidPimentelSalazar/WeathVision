package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.weathvision.Api.ApiClient;
import com.example.weathvision.Api.ApiService;
import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetasAdapter extends RecyclerView.Adapter<MetasAdapter.MetaViewHolder> {

    private List<Metas> listaMetas;
    private Context context;

    public MetasAdapter(Context context, List<Metas> listaMetas) {
        this.context = context;
        this.listaMetas = listaMetas;
    }

    @Override
    public MetaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meta_item, parent, false);
        return new MetaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(MetaViewHolder holder, int position) {
        Metas meta = listaMetas.get(position);
        holder.textFecha.setText(meta.getFechaLimite());
        holder.textTitulo.setText(meta.getTitulo());
        double montoObjetivo = meta.getMontoObjetivo();
        holder.textMonto.setText(String.valueOf((montoObjetivo) + "€"));
        CalcularDiasRestantes(holder.diasRestantes, meta);
        /**
         *
         *  Funcionalidad de click para todas las metas creadas.
         *
         * **/

        holder.itemView.setOnClickListener(v -> {
            showMetaDialog(meta, position);
        });
    }

    /**
     *
     *  Retorna el tamaño de la lista de metas.
     *
     * **/

    @Override
    public int getItemCount() {
        return listaMetas.size();
    }

    /**
     *
     *  Metodo para visualizar los datos en la alerta de la meta.
     *
     *
     * **/

    private void showMetaDialog(Metas meta, int position) {
        /**
         *
         *  Inflar el Layout customizado para la alerta
         *
         *
         * **/
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_view, null);

        /**
         *
         *  Declarar los datos de la alerta, en este caso contamos con:
         *  TITULO: Donde saldrá el titulo principal de la alerta.
         *  FECHA:  Fecha final de la meta.
         *  BALANCE: Muestra el balance de dinero cuando se creó esa alerta
         *  OBJETIVO: Muestra el el objetivo de dinero.
         *  COMENTARIO: Apartado donde el usuario podrá dejar un breve comentario para la alerta.
         *  DIAS RESTANTES: Mostrará cuantos dias quedán en dias. (Ejemplo: 3 días restantes)
         *  BARRA DE PROGRESO: Barra de progreso para ver visualmente los dias restantes.
         *
         * **/
        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView fecha = dialogView.findViewById(R.id.dialog_fecha);
        TextView balance = dialogView.findViewById(R.id.dialog_balance);
        TextView objetivo = dialogView.findViewById(R.id.dialog_objetivo);
        TextView comentario = dialogView.findViewById(R.id.dialog_comentario);
        TextView diasRestantes = dialogView.findViewById(R.id.dias_restantes);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        /**
         *
         *  Mostramos los datos en las variables para su respectiva visualización.
         *
         * **/
        title.setText(meta.getTitulo());
        fecha.setText(meta.getFechaLimite());
        balance.setText( meta.getMonto_actual() + "€");
        objetivo.setText(meta.getMontoObjetivo() + "€");
        comentario.setText(meta.getComentario());


        CalcularDiasRestantes(diasRestantes, meta);

        CalcularBarraDeProgreso(progressBar, meta);

        /**
         *
         *  Crear la alerta y la mostramos.
         *
         * **/

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setView(dialogView)
                .setPositiveButton("Cerrar", (d, which) -> d.dismiss())
                .setNegativeButton("Eliminar", (d, which) -> {
                    // Handle deletion
                    deleteMeta(meta.getIdMeta(), position);
                })
                .create();

        /**
         *
         *  Damos un background personalizado a la alerta, en este caso uno ya realizado en el drawable.
         *
         * **/
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_alert);

        dialog.show();
    }

    private void CalcularBarraDeProgreso(ProgressBar progressBar, Metas meta) {

        double montoActual = meta.getMonto_actual();
        double montoObjetivo = meta.getMontoObjetivo();
        int progress = (montoObjetivo > 0) ? (int) ((montoActual / montoObjetivo) * 100) : 0;
        progressBar.setMax(100);
        progressBar.setProgress(Math.min(progress, 100));

    }

    private void CalcularDiasRestantes(  TextView diasRestantes, Metas meta) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date deadlineDate = sdf.parse(meta.getFechaLimite());
            Date currentDate = new Date();
            long diffInMillies = deadlineDate.getTime() - currentDate.getTime();
            long daysRemaining = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if (daysRemaining >= 0) {
                diasRestantes.setText(daysRemaining + " días restantes");

            } else {
                diasRestantes.setText("Fecha límite vencida");
            }


        } catch (ParseException e) {
            diasRestantes.setText("Error en fecha");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *
     *  Metodo para borrar una meta de la base de datos
     *
     * **/
    private void deleteMeta(int metaId, int position) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteMeta(metaId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listaMetas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaMetas.size());
                    Toast.makeText(context, "Meta eliminada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al eliminar la meta: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class MetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView textFecha, textTitulo, textMonto, diasRestantes;

        public MetaViewHolder(View itemView) {
            super(itemView);
            textFecha = itemView.findViewById(R.id.fecha);
            textTitulo = itemView.findViewById(R.id.tituloMeta);
            textMonto = itemView.findViewById(R.id.montoMeta);
            diasRestantes = itemView.findViewById(R.id.diasRestantes);

        }
    }
}