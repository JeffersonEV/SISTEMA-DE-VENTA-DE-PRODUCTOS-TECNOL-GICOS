package com.sistematecnologico.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 200)
    private String subtitulo;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "enlace_url", length = 500)
    private String enlaceUrl;

    @Column(name = "texto_boton", length = 80)
    private String textoBoton;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
