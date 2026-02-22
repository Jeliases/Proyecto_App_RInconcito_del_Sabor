package com.example.proyecto_rinconcito.admin

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.databinding.ActivityHorarioBinding
import com.example.proyecto_rinconcito.models.DiaHorario
import com.example.proyecto_rinconcito.models.Horario
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HorarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHorarioBinding
    private val db = FirebaseFirestore.getInstance()
    private val horarioRef = db.collection("restaurante").document("horario")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHorarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDay("lunes", binding.switchLunes, binding.layoutHorasLunes, binding.etAperturaLunes, binding.etCierreLunes)
        setupDay("martes", binding.switchMartes, binding.layoutHorasMartes, binding.etAperturaMartes, binding.etCierreMartes)
        setupDay("miercoles", binding.switchMiercoles, binding.layoutHorasMiercoles, binding.etAperturaMiercoles, binding.etCierreMiercoles)
        setupDay("jueves", binding.switchJueves, binding.layoutHorasJueves, binding.etAperturaJueves, binding.etCierreJueves)
        setupDay("viernes", binding.switchViernes, binding.layoutHorasViernes, binding.etAperturaViernes, binding.etCierreViernes)
        setupDay("sabado", binding.switchSabado, binding.layoutHorasSabado, binding.etAperturaSabado, binding.etCierreSabado)
        setupDay("domingo", binding.switchDomingo, binding.layoutHorasDomingo, binding.etAperturaDomingo, binding.etCierreDomingo)
        
        cargarHorario()

        binding.btnGuardarHorario.setOnClickListener { guardarHorario() }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun setupDay(dayName: String, daySwitch: SwitchMaterial, hoursLayout: LinearLayout, openTimeEt: EditText, closeTimeEt: EditText) {
        daySwitch.setOnCheckedChangeListener { _, isChecked ->
            hoursLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        openTimeEt.setOnClickListener { showTimePickerDialog(openTimeEt) }
        closeTimeEt.setOnClickListener { showTimePickerDialog(closeTimeEt) }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)

            editText.setText(SimpleDateFormat("hh:mm a", Locale.US).format(cal.time))
        }
        TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }


    private fun cargarHorario(){
        horarioRef.get().addOnSuccessListener { doc ->
            if(doc.exists()){
                val horario = doc.toObject(Horario::class.java)
                horario?.let { bindHorarioToView(it) }
            } else {
                val defaultHorario = Horario()
                horarioRef.set(defaultHorario)
                bindHorarioToView(defaultHorario)
            }
        }
    }

    private fun guardarHorario(){
        val horario = getViewDataAsHorario()
        horarioRef.set(horario)
            .addOnSuccessListener { Toast.makeText(this, "Horario guardado con éxito", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, "Error al guardar el horario", Toast.LENGTH_SHORT).show() }
    }

    private fun bindHorarioToView(horario: Horario){
        bindDayView(horario.lunes, binding.switchLunes, binding.etAperturaLunes, binding.etCierreLunes)
        bindDayView(horario.martes, binding.switchMartes, binding.etAperturaMartes, binding.etCierreMartes)
        bindDayView(horario.miercoles, binding.switchMiercoles, binding.etAperturaMiercoles, binding.etCierreMiercoles)
        bindDayView(horario.jueves, binding.switchJueves, binding.etAperturaJueves, binding.etCierreJueves)
        bindDayView(horario.viernes, binding.switchViernes, binding.etAperturaViernes, binding.etCierreViernes)
        bindDayView(horario.sabado, binding.switchSabado, binding.etAperturaSabado, binding.etCierreSabado)
        bindDayView(horario.domingo, binding.switchDomingo, binding.etAperturaDomingo, binding.etCierreDomingo)
    }
    
    private fun bindDayView(dia: DiaHorario, daySwitch: SwitchMaterial, openEt: EditText, closeEt: EditText) {
        daySwitch.isChecked = dia.abierto
        openEt.setText(dia.apertura)
        closeEt.setText(dia.cierre)
    }

    private fun getViewDataAsHorario(): Horario {
        return Horario(
            lunes = DiaHorario(binding.switchLunes.isChecked, binding.etAperturaLunes.text.toString(), binding.etCierreLunes.text.toString()),
            martes = DiaHorario(binding.switchMartes.isChecked, binding.etAperturaMartes.text.toString(), binding.etCierreMartes.text.toString()),
            miercoles = DiaHorario(binding.switchMiercoles.isChecked, binding.etAperturaMiercoles.text.toString(), binding.etCierreMiercoles.text.toString()),
            jueves = DiaHorario(binding.switchJueves.isChecked, binding.etAperturaJueves.text.toString(), binding.etCierreJueves.text.toString()),
            viernes = DiaHorario(binding.switchViernes.isChecked, binding.etAperturaViernes.text.toString(), binding.etCierreViernes.text.toString()),
            sabado = DiaHorario(binding.switchSabado.isChecked, binding.etAperturaSabado.text.toString(), binding.etCierreSabado.text.toString()),
            domingo = DiaHorario(binding.switchDomingo.isChecked, binding.etAperturaDomingo.text.toString(), binding.etCierreDomingo.text.toString())
        )
    }
}
