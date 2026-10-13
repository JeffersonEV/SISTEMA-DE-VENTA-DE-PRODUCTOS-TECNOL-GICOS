package com.sistematecnologico.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuracion {

    @Id
    private Long id;

    @Column(name = "nombre_tienda", length = 120)
    private String nombreTienda;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "email_contacto", length = 120)
    private String emailContacto;

    @Column(length = 40)
    private String telefono;

    @Column(length = 40)
    private String whatsapp;

    @Column(length = 200)
    private String direccion;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "twitter_url", length = 500)
    private String twitterUrl;
}
