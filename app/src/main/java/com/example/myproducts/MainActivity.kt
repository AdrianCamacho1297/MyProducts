package com.example.myproducts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.toolbox.JsonObjectRequest
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
import com.example.myproducts.VolleySingleton
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    val IP = "http://192.168.0.9"
    var bandera: Boolean = false
    var idProd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun Agregar(view: View) {
        if (etNomProd.text.toString().isEmpty() ||
            etExistencia.text.toString().isEmpty() ||
            etPrecio.text.toString().isEmpty()
        ) {
            etIdProd.setError("Falta información de Ingresar")
            Toast.makeText(
                this, "Falta información de Ingresar",
                Toast.LENGTH_LONG
            ).show();
            etIdProd.requestFocus()
        } else {
            var jsonEntrada = JSONObject()
            jsonEntrada.put("nomProd", etNomProd.text.toString())
            jsonEntrada.put("existencia", etExistencia.text.toString())
            jsonEntrada.put("precio", etPrecio.text.toString())
            sendRequest(IP + "/WSAndroid/insertProducto.php", jsonEntrada)
        }
    }

    fun getAllProductos(view: View) {
        val wsURL = IP + "/WSAndroid/getProductos.php"
        val admin = AdminBD(this)
        admin.Ejecuta("DELETE FROM productos")
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, wsURL, null,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                val sensadoJson = response.getJSONArray("productos")
                for (i in 0 until sensadoJson.length()) {
                    val idprod = sensadoJson.getJSONObject(i).getString("idProd")
                    val nomprod = sensadoJson.getJSONObject(i).getString("nomProd")
                    val existencia = sensadoJson.getJSONObject(i).getString("existencia")
                    val precio = sensadoJson.getJSONObject(i).getString("precio")
                    val sentencia =
                        "INSERT INTO productos(idProd,nomProd,existencia,precio) VALUES (${idprod}, '${nomprod}',${existencia}, ${precio})"
                    val res = admin.Ejecuta(sentencia)
                }
                Toast.makeText(
                    this,
                    "Productos Cargados ",
                    Toast.LENGTH_LONG
                ).show();
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    this,
                    "Error getAllProductos: " + error.message.toString(),
                    Toast.LENGTH_LONG
                ).show();
                Log.d("Zazueta", error.message.toString())
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun Consultar(view: View) {
        if (etIdProd.text.toString().isEmpty()) {
            etIdProd.setError("Falta ingresar clave del producto")
            Toast.makeText(this, "Falta información del id", Toast.LENGTH_SHORT).show();
            etIdProd.requestFocus()
        } else {
            val admin = AdminBD(this)
            val id: String = etIdProd.text.toString()
            val cur =
                admin.Consulta("SELECT idProd,nomProd,existencia,precio FROM productos WHERE idProd=$id")
            if (cur!!.moveToFirst()) {
                etNomProd.setText(cur.getString(1))
                etExistencia.setText(cur.getString(2))
                etPrecio.setText(cur.getString(3))
            } else {
                Toast.makeText(this, "No existe la Clave del Producto", Toast.LENGTH_LONG).show();
                etIdProd.requestFocus()
            }
        }
    }

    fun Actualiza(view: View) {
        if (etIdProd.text.toString().isEmpty() ||
            etNomProd.text.toString().isEmpty() ||
            etExistencia.text.toString().isEmpty() ||
            etPrecio.text.toString().isEmpty()
        ) {
            etIdProd.setError("Falta información de Ingresar")
            Toast.makeText(this, "Falta información de Ingresar", Toast.LENGTH_SHORT).show();
            etIdProd.requestFocus()
        } else {
            // En Esta Parte Actualiza en el Servidor
            var jsonEntrada = JSONObject()
            jsonEntrada.put("idProd", etIdProd.text.toString())
            jsonEntrada.put("nomProd", etNomProd.text.toString())
            jsonEntrada.put("existencia", etExistencia.text.toString())
            jsonEntrada.put("precio", etPrecio.text.toString())
            sendRequest(IP + "/WSAndroid/updateProducto.php", jsonEntrada)
            // En esta Parte Actualiza en la base local
            val id = etIdProd.text.toString()
            val nom = etNomProd.text.toString()
            val exi = etExistencia.text.toString()
            val pre = etPrecio.text.toString()
            val admin = AdminBD(this)
            val sentencia =
                "UPDATE productos SET nomProd='$nom', existencia=$exi, precio=$pre  WHERE idProd=$id"
            if (admin.Ejecuta(sentencia)) {
                Toast.makeText(this, "Producto Actualizado", Toast.LENGTH_SHORT).show();
                etIdProd.setText("")
                etNomProd.setText("")
                etExistencia.setText("0")
                etPrecio.setText("0")
                etIdProd.requestFocus()
            } else {
                Toast.makeText(this, "Error al Actuaizar", Toast.LENGTH_SHORT).show();
                etIdProd.setText("")
                etNomProd.setText("")
                etExistencia.setText("0")
                etPrecio.setText("0")
                etIdProd.requestFocus()
            }
        }
    }

    fun Borrar(view: View) {
        if (etIdProd.text.toString().isEmpty()) {
            etIdProd.setError("Falta ingresar clave del producto")
            Toast.makeText(this, "Falta información del id", Toast.LENGTH_SHORT).show();
            etIdProd.requestFocus()
        } else {
            // En Esta Parte Elimina en el Servidor
            var jsonEntrada = JSONObject()
            jsonEntrada.put("idProd", etIdProd.text.toString())
            sendRequest(IP + "/WSAndroid/deleteProducto.php", jsonEntrada)
            // En esta Parte Elimina en la base local
            val id = etIdProd.text.toString()
            val admin = AdminBD(this)
            val sentencia = "DELETE FROM productos WHERE idProd=$id"
            if (admin.Ejecuta(sentencia)) {
                Toast.makeText(this, "Producto Eliminado", Toast.LENGTH_SHORT).show();
                etIdProd.setText("")
                etNomProd.setText("")
                etExistencia.setText("0")
                etPrecio.setText("0")
                etIdProd.requestFocus()
            } else {
                Toast.makeText(this, "Error al Eliminar", Toast.LENGTH_SHORT).show();
                etIdProd.setText("")
                etNomProd.setText("")
                etExistencia.setText("0")
                etPrecio.setText("0")
                etIdProd.requestFocus()
            }
        }
    }

    fun Limpiar(view: View) {
        etIdProd.setText("")
        etNomProd.setText("")
        etExistencia.setText("0")
        etPrecio.setText("0")
        etIdProd.requestFocus()
    }

    fun searchIdProd(view: View) {
        val id = etIdProd.text.toString()
        val wsURL = IP + "/WSAndroid/getProducto.php"
        var jsonEntrada = JSONObject()
        jsonEntrada.put("idProd", id)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, wsURL, jsonEntrada,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                if (succ == 200) {
                    val sensadoJson = response.getJSONArray("producto")
                    val idprod = sensadoJson.getJSONObject(0).getString("idProd")
                    val nomprod = sensadoJson.getJSONObject(0).getString("nomProd")
                    val existencia = sensadoJson.getJSONObject(0).getString("existencia")
                    val precio = sensadoJson.getJSONObject(0).getString("precio")
                    etIdProd.setText(idprod)
                    etNomProd.setText(nomprod)
                    etExistencia.setText(existencia)
                    etPrecio.setText(precio)
                }
                Toast.makeText(
                    this,
                    "Producto Encontrado",
                    Toast.LENGTH_LONG
                ).show();
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    this,
                    "Error getAllProductos: " + error.message.toString(),
                    Toast.LENGTH_LONG
                ).show();
                Log.d("Zazueta", error.message.toString())
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun sendRequest(wsURL: String, jsonEnt: JSONObject) {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, wsURL, jsonEnt,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                if (succ == 200) {
                    etIdProd.setText("")
                    etNomProd.setText("")
                    etExistencia.setText("0")
                    etPrecio.setText("0")
                    etIdProd.requestFocus()
                    Toast.makeText(
                        this,
                        "Success:${succ}  Message:${msg}",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show();
                Log.d("ERROR", "${error.message}");
                Toast.makeText(this, "Error de capa 8 checa URL", Toast.LENGTH_SHORT).show();
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}




