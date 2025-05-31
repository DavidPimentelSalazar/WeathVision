package com.example.weathvision.Api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** En esta clase se declara la base de la url donde esta alojada la API **/

public class ApiClient {
    /// "ip local en el emulador 10.0.2.2:8000(para la de python), 10.0.2.2:3000 (para la de nodejs)"
    /// Seleccionadr una url de para ejecutar la bbdd:
    private static final String BASE_URL = "http://192.168.1.114:8000/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
