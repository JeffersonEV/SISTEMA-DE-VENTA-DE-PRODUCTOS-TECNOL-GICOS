package com.sistematecnologico.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias", uniqueConstraints = @UniqueConstraint(name = "uk_categorias_nombre", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id", foreignKey = @ForeignKey(name = "fk_categorias_padre"))
    private Categoria padre;

    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Categoria> subcategorias = new ArrayList<>();

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}
