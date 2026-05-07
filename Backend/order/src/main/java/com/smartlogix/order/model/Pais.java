package com.smartlogix.order.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pais {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String nombre;
}
