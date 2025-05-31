package com.example.weathvision.Api;

import com.example.weathvision.Api.Class.Categoria;
import com.example.weathvision.Api.Class.CategoriaUpdate;
import com.example.weathvision.Api.Class.CorreoRequest;
import com.example.weathvision.Api.Class.LoginResponse;
import com.example.weathvision.Api.Class.Metas;
import com.example.weathvision.Api.Class.Transaction;
import com.example.weathvision.Api.Class.UsuarioLoginRequest;
import com.example.weathvision.Api.Class.UsuarioRegisterRequest;
import com.example.weathvision.Api.Class.UsuarioResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {


    @POST("usuarios/login")
    Call<LoginResponse> postUsuariosLogin(@Body UsuarioLoginRequest usuarioLoginRequest);

    @POST("usuarios/register")
    Call<UsuarioResponse> postUsuariosRegister(@Body UsuarioRegisterRequest usuarioRegisterRequest);
    @PUT("categorias/{id}")
    Call<Categoria> updateCategoria(@Path("id") int idCategoria, @Body Categoria categoria);
    @POST("metas/registerMeta")
    Call<Metas> postMetasRegister(@Body Metas metas);

    @GET("metas/metas")
    Call<List<Metas>> getMetas(@Query("id_usuario") int idUsuario);

    @DELETE("metas/{meta_id}")
    Call<Void> deleteMeta(@Path("meta_id") int metaId);

    @GET("categorias/{idUsuario}")
    Call<List<Categoria>> getCategorias(@Path("idUsuario") int idUsuario);

    @POST("categorias")
    Call<Categoria> createCategoria(@Body Categoria categoria);

    @POST("transacciones/register")
    Call<Transaction> postTransaccion(@Body Transaction transaccion);

    @GET("usuarios/transacciones")
    Call<List<Transaction>> getTransacciones(@Query("id_usuario") int idUsuario);

    @PUT("usuarios/actualizar-categoria/{id_usuario}")
    Call<UsuarioResponse> actualizarCategoria(@Path("id_usuario") int idUsuario, @Body CategoriaUpdate categoria);

    @PUT("/usuarios/actualizar-contrasena/{correo}")
    Call<UsuarioResponse> actualizarContrasena(@Path("correo") String correo, @Body Map<String, String> body);

    @DELETE("transacciones/{idTransaccion}")
    Call<Void> deleteTransaccion(@Path("idTransaccion") int idTransaccion);

    @POST("mail/enviar-correo/")
    Call<Response> enviarCorreo(@Body CorreoRequest request);

    @GET("usuarios/correos")
    Call<List<String>> obtenerCorreos();




}


