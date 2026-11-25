package com.example.projetorevisao.ui.components

/**
 * Estilo do mapa no visual do app (guia visual v1.0): neutros quentes, água azul suave,
 * vias claras e poucos pontos comerciais, para os pinos dos relatos ficarem em destaque.
 *
 * - [GOOGLE_LIGHT] / [GOOGLE_DARK]: JSON de estilo do Google Maps (MapStyleOptions).
 * - [leafletTileFilter]: filtro CSS aplicado aos blocos do OpenStreetMap (mapa alternativo).
 */
internal object MapStyle {

    val GOOGLE_LIGHT = """
        [
          {"elementType":"geometry","stylers":[{"color":"#F6F4EF"}]},
          {"elementType":"labels.text.fill","stylers":[{"color":"#4A463F"}]},
          {"elementType":"labels.text.stroke","stylers":[{"color":"#F6F4EF"}]},
          {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#DDD8CF"}]},
          {"featureType":"administrative.neighborhood","elementType":"labels.text.fill","stylers":[{"color":"#1D3B8B"}]},
          {"featureType":"landscape.man_made","elementType":"geometry","stylers":[{"color":"#EEEBE5"}]},
          {"featureType":"poi","stylers":[{"visibility":"off"}]},
          {"featureType":"poi.park","stylers":[{"visibility":"on"}]},
          {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#DCE8D2"}]},
          {"featureType":"poi.park","elementType":"labels","stylers":[{"visibility":"off"}]},
          {"featureType":"road","elementType":"geometry","stylers":[{"color":"#FFFFFF"}]},
          {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#DDD8CF"}]},
          {"featureType":"road","elementType":"labels.icon","stylers":[{"visibility":"off"}]},
          {"featureType":"road.arterial","elementType":"labels.text.fill","stylers":[{"color":"#7D776D"}]},
          {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#FFDCBD"}]},
          {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#F2C49C"}]},
          {"featureType":"transit","elementType":"labels.icon","stylers":[{"saturation":-60}]},
          {"featureType":"transit.line","elementType":"geometry","stylers":[{"color":"#AFC1FF"}]},
          {"featureType":"water","elementType":"geometry","stylers":[{"color":"#D6E3FF"}]},
          {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#1D3B8B"}]}
        ]
    """.trimIndent()

    val GOOGLE_DARK = """
        [
          {"elementType":"geometry","stylers":[{"color":"#1B1A18"}]},
          {"elementType":"labels.text.fill","stylers":[{"color":"#B8B2A7"}]},
          {"elementType":"labels.text.stroke","stylers":[{"color":"#121110"}]},
          {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#36332F"}]},
          {"featureType":"administrative.neighborhood","elementType":"labels.text.fill","stylers":[{"color":"#AFC1FF"}]},
          {"featureType":"landscape.man_made","elementType":"geometry","stylers":[{"color":"#211F1D"}]},
          {"featureType":"poi","stylers":[{"visibility":"off"}]},
          {"featureType":"poi.park","stylers":[{"visibility":"on"}]},
          {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#22291F"}]},
          {"featureType":"poi.park","elementType":"labels","stylers":[{"visibility":"off"}]},
          {"featureType":"road","elementType":"geometry","stylers":[{"color":"#2C2A27"}]},
          {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#121110"}]},
          {"featureType":"road","elementType":"labels.icon","stylers":[{"visibility":"off"}]},
          {"featureType":"road.arterial","elementType":"labels.text.fill","stylers":[{"color":"#8A847A"}]},
          {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#3F3326"}]},
          {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#121110"}]},
          {"featureType":"transit","elementType":"labels.icon","stylers":[{"saturation":-60},{"lightness":-20}]},
          {"featureType":"transit.line","elementType":"geometry","stylers":[{"color":"#33405F"}]},
          {"featureType":"water","elementType":"geometry","stylers":[{"color":"#1A2333"}]},
          {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#7D8FBF"}]}
        ]
    """.trimIndent()

    /** Filtro dos blocos do OpenStreetMap: tons quentes e menos saturados; no escuro, inverte as cores. */
    fun leafletTileFilter(dark: Boolean): String =
        if (dark) "invert(1) hue-rotate(180deg) brightness(0.88) contrast(0.88) saturate(0.35) sepia(0.12)"
        else "saturate(0.55) sepia(0.14) brightness(1.03) contrast(0.96)"
}
