package com.example.domain.model

enum class CalculationMode(val title: String, val subtitle: String) {
  BY_HOSTS(
    title = "Hosts requeridos",
    subtitle = "Calcular según la cantidad de hosts útiles deseados por subred"
  ),
  BY_MASK(
    title = "Máscara de Subred",
    subtitle = "Calcular según el prefijo CIDR o la máscara de red deseada"
  )
}
